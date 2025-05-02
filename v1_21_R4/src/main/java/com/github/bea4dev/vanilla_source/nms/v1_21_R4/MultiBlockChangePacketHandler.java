package com.github.bea4dev.vanilla_source.nms.v1_21_R4;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.core.SectionPos;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.SectionTypeArray;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;

public class MultiBlockChangePacketHandler implements IPacketHandler {

    public static Field sectionPosField;
    public static Field positionsField;
    public static Field statesField;
    private static final ShortArraySet emptyShortArraySet = new ShortArraySet();
    private static final BlockState[] emptyBlockStateArray = new BlockState[0];

    static {
        try {
            sectionPosField = ClientboundSectionBlocksUpdatePacket.class.getDeclaredField("sectionPos");
            positionsField = ClientboundSectionBlocksUpdatePacket.class.getDeclaredField("positions");
            statesField = ClientboundSectionBlocksUpdatePacket.class.getDeclaredField("states");

            NMSHandler.setRewritable(sectionPosField);
            NMSHandler.setRewritable(positionsField);
            NMSHandler.setRewritable(statesField);
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = EnginePlayer.getUniverse();

        String worldName = EnginePlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
            SectionPos sectionPos = (SectionPos) sectionPosField.get(packet);
            
            int chunkX = sectionPos.x();
            int chunkZ = sectionPos.z();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockDifferenceData()) return packet;
    
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionPos.y());
            if (sectionTypeArray == null) return packet;
            
            short[] cValue = (short[]) positionsField.get(packet);
            BlockState[] dValueClone = ((BlockState[]) statesField.get(packet)).clone();
            
            for (int i = 0; i < cValue.length; i++) {
                short coord = cValue[i];
                
                int x = coord >> 8;
                int y = coord & 0xF;
                int z = (coord >> 4) & 0xF;
                
                BlockState iBlockData = (BlockState) sectionTypeArray.getType(x, y, z);
                if (iBlockData != null) {
                    dValueClone[i] = iBlockData;
                }
            }

            
            var newPacket = new ClientboundSectionBlocksUpdatePacket(sectionPos, emptyShortArraySet, emptyBlockStateArray);
            sectionPosField.set(newPacket, sectionPos);
            positionsField.set(newPacket, cValue);
            statesField.set(newPacket, dValueClone);
            
            return newPacket;

        } catch (Exception e) { e.printStackTrace(); }

        return packet;
    }
}

package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;
import net.minecraft.world.level.block.state.IBlockData;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.SectionTypeArray;

import java.lang.reflect.Field;

public class MultiBlockChangePacketHandler implements IPacketHandler {

    public static Field b;
    public static Field c;
    public static Field d;

    static {
        try{
            b = PacketPlayOutMultiBlockChange.class.getDeclaredField("b");
            c = PacketPlayOutMultiBlockChange.class.getDeclaredField("c");
            d = PacketPlayOutMultiBlockChange.class.getDeclaredField("d");

            b.setAccessible(true);
            c.setAccessible(true);
            d.setAccessible(true);
            
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = EnginePlayer.getUniverse();

        String worldName = EnginePlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
    
            SectionPosition bValue = (SectionPosition) b.get(packet);
            
            int chunkX = bValue.a();
            int chunkZ = bValue.c();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockDifferenceData()) return packet;
    
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(bValue.b());
            if (sectionTypeArray == null) return packet;
            
            short[] cValue = (short[]) c.get(packet);
            IBlockData[] dValueClone = ((IBlockData[]) d.get(packet)).clone();
            
            for (int i = 0; i < cValue.length; i++) {
                short coord = cValue[i];
                
                int x = coord >> 8;
                int y = coord & 0xF;
                int z = (coord >> 4) & 0xF;
                
                IBlockData iBlockData = (IBlockData) sectionTypeArray.getType(x, y, z);
                if (iBlockData != null) {
                    dValueClone[i] = iBlockData;
                }
            }
            
            
            PacketPlayOutMultiBlockChange newPacket = new PacketPlayOutMultiBlockChange(bValue, new ShortArraySet(), new IBlockData[0]);
            b.set(newPacket, bValue);
            c.set(newPacket, cValue);
            d.set(newPacket, dValueClone);
            
            return newPacket;
            
        } catch (Exception e) { e.printStackTrace(); }

        return packet;
    }
}

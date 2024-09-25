package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import com.github.bea4dev.vanilla_source.api.util.SectionLevelArray;
import com.github.bea4dev.vanilla_source.api.world.ChunkUtil;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LightUpdatePacketHandler implements IPacketHandler {

    private static Field chunkInitPacketLightData;

    private static Field lightUpdatePacketData;
    
    private static Field skyYMask;
    private static Field blockYMask;
    private static Field emptySkyYMask;
    private static Field emptyBlockYMask;
    private static Field skyUpdates;
    private static Field blockUpdates;
    
    static {
        try {
            chunkInitPacketLightData = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("lightData");
            NMSHandler.setRewritable(chunkInitPacketLightData);

            lightUpdatePacketData = ClientboundLightUpdatePacket.class.getDeclaredField("lightData");
            NMSHandler.setRewritable(lightUpdatePacketData);
            
            skyYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("skyYMask");
            blockYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("blockYMask");
            emptySkyYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("emptySkyYMask");
            emptyBlockYMask = ClientboundLightUpdatePacketData.class.getDeclaredField("emptyBlockYMask");
            skyUpdates = ClientboundLightUpdatePacketData.class.getDeclaredField("skyUpdates");
            blockUpdates = ClientboundLightUpdatePacketData.class.getDeclaredField("blockUpdates");
            NMSHandler.setRewritable(skyYMask);
            NMSHandler.setRewritable(blockYMask);
            NMSHandler.setRewritable(emptySkyYMask);
            NMSHandler.setRewritable(emptyBlockYMask);
            NMSHandler.setRewritable(skyUpdates);
            NMSHandler.setRewritable(blockUpdates);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {
        
        ParallelUniverse universe = enginePlayer.getUniverse();
    
        World world = enginePlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
    
        boolean isInitChunkPacket = packet instanceof ClientboundLevelChunkWithLightPacket;
    
        try {
            int chunkX;
            int chunkZ;
            ClientboundLightUpdatePacketData lightPacketData;
            
            if (isInitChunkPacket) {
                var chunkPacket = (ClientboundLevelChunkWithLightPacket) packet;
                chunkX = chunkPacket.getX();
                chunkZ = chunkPacket.getZ();
                lightPacketData = chunkPacket.getLightData();
            } else {
                var chunkPacket = (ClientboundLightUpdatePacket) packet;
                chunkX = chunkPacket.getX();
                chunkZ = chunkPacket.getZ();
                lightPacketData = chunkPacket.getLightData();
            }
    
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockLightLevelDifferenceData() && !parallelChunk.hasSkyLightLevelDifferenceData()) {
                return packet;
            }
            
            Object cachedPacketData = parallelChunk.getCachedLightUpdatePacket();
            if (cachedPacketData != null){
                if (isInitChunkPacket) {
                    chunkInitPacketLightData.set(packet, cachedPacketData);
                } else {
                    lightUpdatePacketData.set(packet, cachedPacketData);
                }
                return packet;
            }
    
            BitSet skyMaskData = (BitSet) skyYMask.get(lightPacketData);
            BitSet blockMaskData = (BitSet) blockYMask.get(lightPacketData);
            BitSet emptySkyMaskData = (BitSet) emptySkyYMask.get(lightPacketData);
            BitSet emptyBlockMaskData = (BitSet) emptyBlockYMask.get(lightPacketData);
            List<byte[]> skyUpdatesData = (List<byte[]>) skyUpdates.get(lightPacketData);
            List<byte[]> blockUpdatesData = (List<byte[]>) blockUpdates.get(lightPacketData);

            ServerLevel worldServer = ((CraftWorld) world).getHandle();
            LevelLightEngine levelLightEngine = worldServer.getLightEngine();

            int sectionCount = levelLightEngine.getLightSectionCount();
            int minSection = levelLightEngine.getMinLightSection();
            
            List<byte[]> newSkyUpdates = new ArrayList<>();
            List<byte[]> newBlockUpdates = new ArrayList<>();
            int skyUpdateIndex = 0;
            int blockUpdateIndex = 0;
            
            for (int i = 0; i < sectionCount; i++) {
                int sectionY = minSection + i;
                boolean hasSkySection = skyMaskData.get(i);
                boolean hasBlockSection = blockMaskData.get(i);
                SectionLevelArray parallelSky = null;
                SectionLevelArray parallelBlock = null;
                
                if (ChunkUtil.isInRangeHeight(sectionY << 4)) {
                    parallelSky = parallelChunk.getSkyLightSectionLevelArray(sectionY);
                    parallelBlock = parallelChunk.getBlockLightSectionLevelArray(sectionY);
                }
                
                if (hasSkySection) {
                    byte[] data = skyUpdatesData.get(skyUpdateIndex);
                    skyUpdateIndex++;
                    
                    if (parallelSky != null) {
                        DataLayer nibbleArray = new DataLayer(data);
                        parallelSky.threadsafeIteration(nibbleArray::set);
                        data = nibbleArray.getData();
                    }
                    
                    newSkyUpdates.add(data);
                } else {
                    if (parallelSky != null) {
                        DataLayer nibbleArray = new DataLayer(new byte[2048]);
                        parallelSky.threadsafeIteration(nibbleArray::set);
                        newSkyUpdates.add(nibbleArray.getData());
                        
                        skyMaskData.set(sectionY);
                        emptySkyMaskData.set(sectionY, false);
                    }
                }
    
                if (hasBlockSection) {
                    byte[] data = blockUpdatesData.get(blockUpdateIndex);
                    blockUpdateIndex++;
        
                    if (parallelBlock != null) {
                        DataLayer nibbleArray = new DataLayer(data);
                        parallelBlock.threadsafeIteration(nibbleArray::set);
                        data = nibbleArray.getData();
                    }
        
                    newBlockUpdates.add(data);
                } else {
                    if (parallelBlock != null) {
                        DataLayer nibbleArray = new DataLayer(new byte[2048]);
                        parallelBlock.threadsafeIteration(nibbleArray::set);
                        newBlockUpdates.add(nibbleArray.getData());
            
                        blockMaskData.set(sectionY);
                        emptyBlockMaskData.set(sectionY, false);
                    }
                }
            }
            
            skyUpdates.set(lightPacketData, newSkyUpdates);
            blockUpdates.set(lightPacketData, newBlockUpdates);
            
            if(cacheSetting) parallelChunk.setLightUpdatePacketCache(lightPacketData);
            
            return packet;
            
        } catch (Exception e) { e.printStackTrace(); }
        
        return packet;
    }
}

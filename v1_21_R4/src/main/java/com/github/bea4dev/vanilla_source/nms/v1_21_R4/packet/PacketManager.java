package com.github.bea4dev.vanilla_source.nms.v1_21_R4.packet;

import com.github.bea4dev.vanilla_source.nms.v1_21_R4.NMSHandler;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.util.BlockPosition3i;
import com.github.bea4dev.vanilla_source.api.util.SectionLevelArray;
import com.github.bea4dev.vanilla_source.api.util.SectionTypeArray;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R4.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PacketManager {
    
    public static @Nullable Object createBlockChangePacket(
            ParallelWorld parallelWorld,
            int blockX,
            int blockY,
            int blockZ
    ) {
        BlockData blockData = parallelWorld.getBlockData(blockX, blockY, blockZ);
        if (blockData == null) return null;
        
        return new ClientboundBlockUpdatePacket(
                new BlockPos(blockX, blockY, blockZ),
                ((CraftBlockData) blockData).getState()
        );
    }
    
    
    public static Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks) {
        Map<BlockPosition3i, Set<BlockPosition3i>> chunkMap = new HashMap<>();
        
        for(BlockPosition3i bp : blocks){
            chunkMap.computeIfAbsent(new BlockPosition3i(bp.getX() >> 4, bp.getY() >> 4, bp.getZ() >> 4), cp -> new HashSet<>()).add(bp);
        }
        
        Set<Object> packets = new HashSet<>();
        
        for(Map.Entry<BlockPosition3i, Set<BlockPosition3i>> entry : chunkMap.entrySet()){
            BlockPosition3i sectionPosition = entry.getKey();
            Set<BlockPosition3i> bps = entry.getValue();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(sectionPosition.getX(), sectionPosition.getZ());
            if(parallelChunk == null) continue;
            
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionPosition.getY());
            if(sectionTypeArray == null) continue;
            
            ShortSet coordList = new ShortArraySet();
            List<BlockState> dataList = new ArrayList<>();
            
            for(BlockPosition3i bp : bps){
                BlockState iBlockData = (BlockState) sectionTypeArray.getType(
                        bp.getX() & 0xF,
                        bp.getY() & 0xF,
                        bp.getZ() & 0xF
                );
                if(iBlockData == null) continue;
                
                coordList.add((short) ((bp.getX() & 0xF) << 8 | (bp.getZ() & 0xF) << 4 | bp.getY() & 0xF));
                dataList.add(iBlockData);
            }
            
            BlockState[] dataArray = new BlockState[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                dataArray[i] = dataList.get(i);
            }
    
            SectionPos sectionPos = SectionPos.of(sectionPosition.getX(), sectionPosition.getY(), sectionPosition.getZ());
            
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionPos, coordList, dataArray);
            packets.add(packet);
        }
        
        return packets;
    }
    
    
    public static void sendChunkMultiBlockChangeUpdatePacket(
            Player player,
            ParallelChunk parallelChunk,
            NMSHandler nmsHandler
    ) {
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
            
            ShortSet coordList = new ShortArraySet();
            List<BlockState> dataList = new ArrayList<>();
            
            boolean hasValue = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                dataList.add((BlockState) iBlockData);
            });
            
            if(!hasValue) continue;
            
            BlockState[] dataArray = new BlockState[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                dataArray[i] = dataList.get(i);
            }
            
            SectionPos sectionPos = SectionPos.of(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ());
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionPos, coordList, dataArray);
            nmsHandler.sendPacket(player, packet);
        }
    }
    
    
    public static @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
        
        org.bukkit.World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if (world == null) return null;
        
        boolean has = false;
        for (int sectionIndex = 0; sectionIndex < 16; sectionIndex++) {
            SectionLevelArray blockLevelArray = parallelChunk.getBlockLightSectionLevelArray(sectionIndex);
            SectionLevelArray skyLevelArray = parallelChunk.getSkyLightSectionLevelArray(sectionIndex);
            
            if(blockLevelArray != null){
                if(blockLevelArray.getSize() != 0) has = true;
            }
            if(skyLevelArray != null){
                if(skyLevelArray.getSize() != 0) has = true;
            }
        }
        if (!has) return null;
    
        LevelChunk chunk = (LevelChunk) ((CraftChunk) world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ()))
                .getHandle(ChunkStatus.FULL);
        if (chunk == null) return null;
        
        return new ClientboundLightUpdatePacket(
                new ChunkPos(parallelChunk.getChunkX(), parallelChunk.getChunkZ()),
                ((CraftWorld) world).getHandle().getLightEngine(),
                null,
                null
        );
    }
    
    
    public static void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk, NMSHandler nmsHandler) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
        
        org.bukkit.World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if (world == null) return;
        
        if (player.getWorld() != world) return;
        
        org.bukkit.Chunk chunk = world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ());
        LevelChunk nmsChunk = (LevelChunk) ((CraftChunk) chunk).getHandle(ChunkStatus.FULL);
        
        for (int sectionIndex = 0; sectionIndex < 16; sectionIndex++) {
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if (sectionTypeArray == null) continue;
            
            LevelChunkSection chunkSection = nmsChunk.getSections()[sectionIndex];
            if (chunkSection == null) continue;
            
            ShortSet coordList = new ShortArraySet();
            List<BlockState> dataList = new ArrayList<>();
            boolean hasValue = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                
                BlockState chunkData = chunkSection.getBlockState(x, y, z);
                if(chunkData == null) chunkData = ((CraftBlockData) org.bukkit.Material.AIR.createBlockData()).getState();
                dataList.add(chunkData);
            });
            if (!hasValue) continue;
            
            BlockState[] dataArray = new BlockState[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                dataArray[i] = dataList.get(i);
            }
            
            SectionPos sectionPos = SectionPos.of(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ());
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionPos, coordList, dataArray);
            nmsHandler.sendPacket(player, packet);
        }
    }
    
}

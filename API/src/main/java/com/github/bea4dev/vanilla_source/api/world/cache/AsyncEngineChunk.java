package com.github.bea4dev.vanilla_source.api.world.cache;

import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.world.ChunkUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncEngineChunk implements EngineChunk {

    private static final Set<EngineEntity> EMPTY_ENTITY_LIST = Collections.emptySet();

    
    private @Nullable ChunkSnapshot chunkSnapshot;
    
    private final int chunkX;
    
    private final int chunkZ;
    
    private final Set<EngineEntity>[] entitySlices;
    
    public AsyncEngineChunk(Chunk chunk) {
        this.chunkSnapshot = chunk.getChunkSnapshot(true, true, true);
        this.chunkX = chunkSnapshot.getX();
        this.chunkZ = chunkSnapshot.getZ();
        
        int index = VanillaSourceAPI.getInstance().isHigher_v1_18_R1() ? 24 : 16;
        this.entitySlices = new Set[index];
        for(int i = 0; i < index; i++){
            entitySlices[i] = ConcurrentHashMap.newKeySet();
        }
    }

    public AsyncEngineChunk(int chunkX, int chunkZ) {
        this.chunkSnapshot = null;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        int index = VanillaSourceAPI.getInstance().isHigher_v1_18_R1() ? 24 : 16;
        this.entitySlices = new Set[index];
        for(int i = 0; i < index; i++){
            entitySlices[i] = ConcurrentHashMap.newKeySet();
        }
    }
    
    @Override
    public int getChunkX() {return chunkX;}
    
    @Override
    public int getChunkZ() {return chunkZ;}
    
    @Override
    public @NotNull Set<EngineEntity> getEntitiesInSection(int sectionIndex) {
        if(isOutOfSectionIndex(sectionIndex)) return EMPTY_ENTITY_LIST;
        return entitySlices[sectionIndex];
    }
    
    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        if (chunkSnapshot == null) return null;
        if (!ChunkUtil.isInRangeHeight(blockY)) return null;

        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
        if(isOutOfSectionIndex(sectionIndex)) return null;
        if(chunkSnapshot.isSectionEmpty(sectionIndex)) return null;
        return chunkSnapshot.getBlockType(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        if (chunkSnapshot == null) return null;
        if (!ChunkUtil.isInRangeHeight(blockY)) return null;

        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
        if(isOutOfSectionIndex(sectionIndex)) return null;
        if(chunkSnapshot.isSectionEmpty(sectionIndex)) return null;
        return chunkSnapshot.getBlockData(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        if (chunkSnapshot == null) return null;
        if (!ChunkUtil.isInRangeHeight(blockY)) return null;

        BlockData blockData = getBlockData(blockX, blockY, blockZ);
        if(blockData == null) return null;
        
        return VanillaSourceAPI.getInstance().getNMSHandler().getIBlockData(blockData);
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        if (chunkSnapshot == null) return false;
        if (!ChunkUtil.isInRangeHeight(blockY)) return false;

        return getBlockData(blockX, blockY, blockZ) != null;
    }
    
    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        if (chunkSnapshot == null) return 0;
        if (!ChunkUtil.isInRangeHeight(blockY)) return 0;

        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
        if(isOutOfSectionIndex(sectionIndex)) return 0;
        if(chunkSnapshot.isSectionEmpty(sectionIndex)) return 0;
        return chunkSnapshot.getBlockEmittedLight(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        if (chunkSnapshot == null) return 0;
        if (!ChunkUtil.isInRangeHeight(blockY)) return 0;

        int sectionIndex = ChunkUtil.getSectionIndexAligned(blockY);
        if (isOutOfSectionIndex(sectionIndex)) return 0;
        if (chunkSnapshot.isSectionEmpty(sectionIndex)) return 0;
        return chunkSnapshot.getBlockSkyLight(blockX & 0xF, blockY, blockZ & 0xF);
    }
    
    @Override
    public @Nullable ChunkSnapshot getChunkSnapShot() {return chunkSnapshot;}

    @Override
    public boolean isLoaded() {
        return chunkSnapshot != null;
    }

    public void update(Chunk chunk){this.chunkSnapshot = chunk.getChunkSnapshot(true, true, true);}

    private boolean isOutOfSectionIndex(int sectionIndex){
        return sectionIndex < 0 || sectionIndex >= entitySlices.length;
    }

}

package com.github.bea4dev.vanilla_source.api.world.cache.local;

import com.github.bea4dev.vanilla_source.api.entity.tick.TickThread;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.evaluators.ClassBlock;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.world.ChunkUtil;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncEngineChunk;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncEngineWorld;
import com.github.bea4dev.vanilla_source.api.world.cache.EngineWorld;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalEngineWorld implements EngineWorld {

    private final String worldName;

    private final AsyncEngineWorld asyncWorld;

    private final Long2ObjectOpenHashMap<AsyncEngineChunk> chunkMap = new Long2ObjectOpenHashMap<>();

    private final ContanClassInstance scriptHandle;

    public ThreadLocalEngineWorld(String worldName, AsyncEngineWorld asyncWorld, TickThread tickThread) {
        this.worldName = worldName;
        this.asyncWorld = asyncWorld;

        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();

        ContanClassInstance scriptHandle = null;
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/world/World.cntn");
        if (contanModule != null) {
            ClassBlock classBlock = contanModule.getClassByName("World");
            if (classBlock != null) {
                scriptHandle = classBlock.createInstance(contanEngine, tickThread, new JavaClassInstance(contanEngine, this));
            }
        }
        this.scriptHandle = scriptHandle;
    }

    @Override
    public String getName() {
        return worldName;
    }

    @Override
    public Material getType(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        return asyncChunk.getType(x, y, z);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        return asyncChunk.getBlockData(x, y, z);
    }

    @Override
    public @Nullable Object getNMSBlockData(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        return asyncChunk.getNMSBlockData(x, y, z);
    }

    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        AsyncEngineChunk asyncChunk = getChunkAt(blockX >> 4, blockZ >> 4);
        return asyncChunk.hasBlockData(blockX, blockY, blockZ);
    }


    @Override
    public int getBlockLightLevel(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        return asyncChunk.getBlockLightLevel(x, y, z);
    }

    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        AsyncEngineChunk asyncChunk = getChunkAt(x >> 4, z >> 4);
        return asyncChunk.getSkyLightLevel(x, y, z);
    }

    @Override
    public @NotNull AsyncEngineChunk getChunkAt(int chunkX, int chunkZ) {
        long coord = ChunkUtil.getChunkKey(chunkX, chunkZ);
        AsyncEngineChunk asyncChunk = chunkMap.get(coord);
        if (asyncChunk == null) {
            asyncChunk = asyncWorld.getChunkAt(chunkX, chunkZ);
            chunkMap.put(coord, asyncChunk);
        }
        return asyncChunk;
    }

    @Override
    public @NotNull ContanClassInstance getScriptHandle() {
        return scriptHandle;
    }

    public void releaseChunk(Chunk chunk) {
        var chunkKey = ChunkUtil.getChunkKey(chunk.getX(), chunk.getZ());
        var engineChunk = chunkMap.get(chunkKey);
        if (engineChunk != null) {
            engineChunk.unload();
        }

        chunkMap.remove(chunkKey);
    }
}

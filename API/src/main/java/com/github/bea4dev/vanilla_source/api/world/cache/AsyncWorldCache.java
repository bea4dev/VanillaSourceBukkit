package com.github.bea4dev.vanilla_source.api.world.cache;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncWorldCache {
    
    
    private static final Map<String, AsyncEngineWorld> worldMap = new ConcurrentHashMap<>();
    
    /**
     * Register chunk cache to be able to get from async thread.
     */
    public static void register(Chunk chunk) {
        worldMap.computeIfAbsent(chunk.getWorld().getName(), AsyncEngineWorld::new).setChunk(chunk);
    }
    
    public static void update(Chunk chunk) {
        worldMap.computeIfAbsent(chunk.getWorld().getName(), AsyncEngineWorld::new).update(chunk);
    }

    public static void release(Chunk chunk) {
        var world = worldMap.get(chunk.getWorld().getName());
        if (world != null) {
            world.release(chunk);
        }

        for (var thread : VanillaSourceAPI.getInstance().getTickThreadPool().getAsyncTickRunnerList()) {
            thread.releaseChunk(chunk);
        }
    }
    
    /**
     * Get thread safe chunk cache.
     */
    public static @NotNull AsyncEngineWorld getAsyncWorld(String world){
        return worldMap.computeIfAbsent(world, AsyncEngineWorld::new);
    }
    
    public static ChunkSnapshot getChunkCache(String worldName, int chunkX, int chunkZ){
        AsyncEngineWorld asyncWorld = getAsyncWorld(worldName);
        
        AsyncEngineChunk asyncChunk = asyncWorld.getChunkAt(chunkX, chunkZ);
        if(asyncChunk == null) return null;
        
        return asyncChunk.getChunkSnapShot();
    }
    
}

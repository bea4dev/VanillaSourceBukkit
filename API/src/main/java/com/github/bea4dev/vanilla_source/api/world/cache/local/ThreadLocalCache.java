package com.github.bea4dev.vanilla_source.api.world.cache.local;

import com.github.bea4dev.vanilla_source.api.entity.tick.TickThread;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncWorldCache;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;

import java.util.HashMap;
import java.util.Map;

/**
 * Limit use to a single thread to reduce thread locks.
 */
public class ThreadLocalCache {
    
    private final TickThread tickThread;
    
    private final Map<String, ThreadLocalEngineWorld> globalWorld = new HashMap<>();

    private final Map<String, Map<String, ThreadLocalParallelWorld>> parallelWorldMap = new HashMap<>();
    
    public ThreadLocalCache(TickThread tickThread) {
        this.tickThread = tickThread;
    }
    
    public @NotNull ThreadLocalEngineWorld getGlobalWorld(String worldName){
        return globalWorld.computeIfAbsent(worldName, wn -> new ThreadLocalEngineWorld(wn, AsyncWorldCache.getAsyncWorld(wn), tickThread));
    }

    public @NotNull ThreadLocalParallelWorld getParallelWorld(ParallelUniverse universe, String worldName) {
        return parallelWorldMap.computeIfAbsent(universe.getName(), un -> new HashMap<>())
                .computeIfAbsent(worldName, wn -> new ThreadLocalParallelWorld(wn, universe.getWorld(wn), tickThread));
    }

    public void releaseChunk(Chunk chunk) {
        var world = globalWorld.get(chunk.getWorld().getName());
        if (world != null) {
            world.releaseChunk(chunk);
        }
    }
    
}

package com.github.bea4dev.vanilla_source.listener;

import com.github.bea4dev.vanilla_source.VanillaSource;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncWorldCache;
import com.github.bea4dev.vanilla_source.util.TaskHandler;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkListener implements Listener {

    private final Set<Chunk> updateChunks = new HashSet<>();

    public ChunkListener() {
        Bukkit.getScheduler().runTaskTimer(VanillaSource.getPlugin(), () -> {
            for (var chunk : updateChunks) {
                TaskHandler.runWorldSync(chunk.getWorld(), () -> AsyncWorldCache.update(chunk));
            }
            updateChunks.clear();
        }, 0, 5);
    }

    private void registerUpdateChunk(Chunk chunk) {
        for (var x = chunk.getX() - 1; x <= chunk.getX() + 1; x++) {
            for (var z = chunk.getZ() - 1; z <= chunk.getZ() + 1; z++) {
                var newChunk = chunk.getWorld().getChunkAt(x, z);
                updateChunks.add(newChunk);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        AsyncWorldCache.register(chunk);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Chunk chunk = event.getBlock().getChunk();
        registerUpdateChunk(chunk);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Chunk chunk = event.getBlock().getChunk();
        registerUpdateChunk(chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        AsyncWorldCache.release(chunk);
    }
    
}

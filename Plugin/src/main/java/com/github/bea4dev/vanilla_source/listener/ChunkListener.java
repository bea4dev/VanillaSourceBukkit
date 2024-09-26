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
import java.util.List;

public class ChunkListener implements Listener {

    private final List<Chunk> updateChunks = new ArrayList<>();

    public ChunkListener() {
        Bukkit.getScheduler().runTaskTimer(VanillaSource.getPlugin(), () -> {
            for (var chunk : updateChunks) {
                TaskHandler.runWorldSync(chunk.getWorld(), () -> AsyncWorldCache.update(chunk));
            }
            updateChunks.clear();
        }, 0, 5);
    }

    private void registerUpdateChunk(Chunk chunk) {
        if (updateChunks.contains(chunk)) {
            return;
        }
        updateChunks.add(chunk);
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

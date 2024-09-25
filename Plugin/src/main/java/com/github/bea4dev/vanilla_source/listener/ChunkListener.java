package com.github.bea4dev.vanilla_source.listener;

import com.github.bea4dev.vanilla_source.api.world.cache.AsyncWorldCache;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        AsyncWorldCache.register(chunk);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Chunk chunk = event.getBlock().getChunk();
        AsyncWorldCache.update(chunk);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Chunk chunk = event.getBlock().getChunk();
        AsyncWorldCache.update(chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        AsyncWorldCache.release(chunk);
    }
    
}

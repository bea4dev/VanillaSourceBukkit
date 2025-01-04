package com.github.bea4dev.vanilla_source.listener;

import com.github.bea4dev.vanilla_source.api.world.BukkitWorldRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        var world = event.getWorld();
        BukkitWorldRegistry.registerWorld(world.getName(), world);
    }
}

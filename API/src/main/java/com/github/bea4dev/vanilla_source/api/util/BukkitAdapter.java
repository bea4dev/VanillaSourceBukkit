package com.github.bea4dev.vanilla_source.api.util;

import com.github.bea4dev.vanilla_source.api.world.EngineLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BukkitAdapter {
    
    public static Location toBukkitLocation(EngineLocation location) {
        return new Location(location.getWorld() == null ? null : Bukkit.getWorld(location.getWorld().getName()), location.getX(), location.getY(), location.getZ());
    }
    
}

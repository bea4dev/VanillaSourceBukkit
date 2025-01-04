package com.github.bea4dev.vanilla_source.api.world;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitWorldRegistry {
    private static final Map<String, World> worldMap = new ConcurrentHashMap<>();

    public static @Nullable World getWorld(String name) {
        return worldMap.get(name);
    }

    public static void registerWorld(String name, World world) {
        worldMap.put(name, world);
    }
}

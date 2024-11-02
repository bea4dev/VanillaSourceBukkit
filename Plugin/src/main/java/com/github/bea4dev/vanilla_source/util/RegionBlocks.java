package com.github.bea4dev.vanilla_source.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RegionBlocks {
    private final World world;
    
    private final Vector maximum;
    private final Vector minimum;
    
    public RegionBlocks(Location firstPoint, Location secondPoint) {
        world = firstPoint.getWorld();
        Vector firstVector = firstPoint.toVector();
        Vector secondVector = secondPoint.toVector();
        maximum = Vector.getMaximum(firstVector, secondVector);
        minimum = Vector.getMinimum(firstVector, secondVector);
    }
    
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (int y = minimum.getBlockY(); y <= maximum.getBlockY(); y++) {
            for (int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++) {
                for (int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }
        
        return blocks;
    }

    public Vector getMaximum() {return maximum;}

    public Vector getMinimum() {return minimum;}

    public void apply(Consumer<Block> consumer) {
        for (int y = minimum.getBlockY(); y <= maximum.getBlockY(); y++) {
            for (int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++) {
                for (int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    consumer.accept(block);
                }
            }
        }
    }
}

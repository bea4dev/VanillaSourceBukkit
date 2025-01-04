package com.github.bea4dev.vanilla_source.api.asset;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class AssetsWorldChunkGenerator extends ChunkGenerator {
    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        BlockData block;
        if ((chunkX + chunkZ) % 2 == 0) {
            block = Material.WHITE_CONCRETE.createBlockData();
        } else {
            block = Material.LIGHT_BLUE_CONCRETE.createBlockData();
        }

        for (var x = 0; x < 16; x++) {
            for (var z = 0; z < 16; z++) {
                chunkData.setBlock(x, 0, z, block);
            }
        }

        if (chunkX == 0 && chunkZ == 0) {
            chunkData.setBlock(0, 0, 0, Material.BEDROCK);
        }
    }
}

package com.github.bea4dev.vanilla_source.api.asset;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;

public record JigsawState(NamespacedKey name, int priority, BlockFace direction) {}

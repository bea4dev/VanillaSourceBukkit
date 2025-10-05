package com.github.bea4dev.vanilla_source.api.asset;

import org.bukkit.util.Vector;

public record JigsawReference(WorldAsset worldAsset, Vector relativePosition, JigsawState jigsawState) {}
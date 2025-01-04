package com.github.bea4dev.vanilla_source.api.world.cache;

import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBlockBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import com.github.bea4dev.vanilla_source.api.world.block.EngineBlock;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EngineRayTraceResult(EngineBoundingBox hitBoundingBox, Vector hitPosition, BlockFace hitFace) {

    public EngineRayTraceResult(@NotNull EngineBoundingBox hitBoundingBox, @NotNull Vector hitPosition, @NotNull BlockFace hitFace) {
        this.hitBoundingBox = hitBoundingBox;
        this.hitPosition = hitPosition;
        this.hitFace = hitFace;
    }

    @Override
    public @NotNull BlockFace hitFace() {
        return hitFace;
    }

    @Override
    public @NotNull EngineBoundingBox hitBoundingBox() {
        return hitBoundingBox;
    }

    @Override
    public @NotNull Vector hitPosition() {
        return hitPosition.clone();
    }

    public @Nullable EngineEntity getHitEntity() {
        if (!(hitBoundingBox instanceof EngineEntityBoundingBox)) return null;
        return ((EngineEntityBoundingBox) hitBoundingBox).getEntity();
    }

    public @Nullable EngineBlock getHitBlock() {
        if (!(hitBoundingBox instanceof EngineBlockBoundingBox)) return null;
        return ((EngineBlockBoundingBox) hitBoundingBox).getBlock();
    }

}

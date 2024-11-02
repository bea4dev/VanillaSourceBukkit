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

public class EngineRayTraceResult {
    
    private final EngineBoundingBox hitBoundingBox;
    
    private final Vector hitPosition;
    
    private final BlockFace hitFace;
    
    public EngineRayTraceResult(@NotNull EngineBoundingBox hitBoundingBox, @NotNull Vector hitPosition, @NotNull BlockFace hitFace) {
        this.hitBoundingBox = hitBoundingBox;
        this.hitPosition = hitPosition;
        this.hitFace = hitFace;
    }
    
    public @NotNull BlockFace getHitFace() {return hitFace;}
    
    public @NotNull EngineBoundingBox getHitBoundingBox() {return hitBoundingBox;}
    
    public @NotNull Vector getHitPosition() {return hitPosition.clone();}

    public @Nullable EngineEntity getHitEntity() {
        if(!(hitBoundingBox instanceof EngineEntityBoundingBox)) return null;
        return ((EngineEntityBoundingBox) hitBoundingBox).getEntity();
    }

    public @Nullable EngineBlock getHitBlock() {
        if(!(hitBoundingBox instanceof EngineBlockBoundingBox)) return null;
        return ((EngineBlockBoundingBox) hitBoundingBox).getBlock();
    }
    
}

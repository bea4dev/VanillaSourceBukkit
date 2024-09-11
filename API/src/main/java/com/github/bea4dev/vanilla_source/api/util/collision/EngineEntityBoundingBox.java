package com.github.bea4dev.vanilla_source.api.util.collision;

import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import org.jetbrains.annotations.NotNull;

public class EngineEntityBoundingBox extends EngineBoundingBox{
    
    private final EngineEntity entity;
    
    public EngineEntityBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, @NotNull EngineEntity entity) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.entity = entity;
    }
    
    public @NotNull EngineEntity getEntity() {return entity;}
    
    @Override
    public @NotNull EngineEntityBoundingBox clone() {
        return (EngineEntityBoundingBox) super.clone();
    }
}

package com.github.bea4dev.vanilla_source.api.camera;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;

public class LookAtEntityTracker implements CameraPositions {
    
    private final EngineEntity entity;
    
    public LookAtEntityTracker(EngineEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public Vector getTickPosition(int tick) {
        return entity.getPosition().clone();
    }
    
    @Override
    public int getEndTick() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public void save(YamlConfiguration yml) {
        //None
    }
    
    @Override
    public void load(YamlConfiguration yml) {
        //None
    }
    
}

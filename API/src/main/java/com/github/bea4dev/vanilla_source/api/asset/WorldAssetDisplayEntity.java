package com.github.bea4dev.vanilla_source.api.asset;

import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.entity.controller.EntityController;
import com.github.bea4dev.vanilla_source.api.entity.tick.TickThread;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import com.github.bea4dev.vanilla_source.api.world.cache.EngineWorld;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldAssetDisplayEntity extends EngineEntity {
    public final WorldAsset asset;

    /**
     * Create entity instance.
     *
     * @param world            World in which this entity exists
     * @param entityController NMS handle
     * @param tickThread       {@link TickThread} that executes the processing of this entity
     * @param scriptHandle     Contan script handle
     */
    public WorldAssetDisplayEntity(
            WorldAsset asset,
            @NotNull EngineWorld world,
            @NotNull EntityController entityController,
            @NotNull TickThread tickThread,
            @Nullable ContanClassInstance scriptHandle) {
        super(world, entityController, tickThread, scriptHandle);
        this.asset = asset;
    }

    @Override
    public @Nullable EngineEntityBoundingBox getBoundingBox() {
        var start = asset.getStartPosition();
        var end = asset.getEndPosition();

        return new EngineEntityBoundingBox(
                start.getX(),
                start.getY(),
                start.getZ(),
                end.getX(),
                end.getY(),
                end.getZ(),
                this
        );
    }
}

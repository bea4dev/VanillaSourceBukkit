package com.github.bea4dev.vanilla_source.api.entity.model;

import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.setting.VSSettings;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.RenderParsers;
import org.bukkit.Location;
import org.bukkit.World;

public class ModeledEntityHolder {
    private final World world;
    private final Dummy<?> dummy;
    private final ModeledEntity modeledEntity;
    private final RenderParsers renderParsers;

    public ModeledEntityHolder(Location location) {
        world = location.getWorld();
        dummy = new Dummy<>();
        dummy.setLocation(location);
        dummy.setDetectingPlayers(false);
        modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        renderParsers = ModelEngineAPI.getNMSHandler().createParsers();
    }

    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public void tick(EngineEntity entity) {
        var yawPitch = entity.getRotation();
        dummy.syncLocation(entity.getPosition().toLocation(world, yawPitch.x, yawPitch.y));

        if (VSSettings.isOverrideModelEngineUpdater()) {
            dummy.getData().asyncUpdate();
            modeledEntity.tick();

            for (var activeModel : modeledEntity.getModels().values()) {
                if (!activeModel.getModelRenderer().isInitialized()) {
                    return;
                }
                activeModel.getModelRenderer().sendToClient(renderParsers);
            }

            dummy.getData().cleanup();
        }
    }

    public void show(EnginePlayer player) {
        dummy.setForceViewing(player.getBukkitPlayer(), true);
    }

    public void hide(EnginePlayer player) {
        dummy.setForceHidden(player.getBukkitPlayer(), true);
    }

}

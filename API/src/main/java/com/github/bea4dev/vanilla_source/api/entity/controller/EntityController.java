package com.github.bea4dev.vanilla_source.api.entity.controller;

import com.github.bea4dev.vanilla_source.api.entity.tick.EntityTracker;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.math.Vec2f;

public interface EntityController {
    
    /**
     * Set entity position.
     * This method only changes the numerical values of the coordinates and does not cause chunk loading.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    void setPositionRaw(double x, double y, double z);
    
    /**
     * Set entity rotation.
     * @param yaw yaw
     * @param pitch pitch
     */
    void setRotation(float yaw, float pitch);
    
    /**
     * Get entity position.
     * @return Vector(x, y, z)
     */
    Vector getPosition();
    
    /**
     * Get entity rotation.
     * @return Vec2f(yaw, pitch)
     */
    Vec2f getYawPitch();
    
    /**
     * Get entity bounding box.
     * @return {@link EngineEntityBoundingBox}
     */
    EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity);
    
    /**
     * Recalculate the BoundingBox after the entity has moved.
     * @param boundingBox {@link EngineBoundingBox}
     */
    void resetBoundingBoxForMovement(EngineBoundingBox boundingBox);
    
    /**
     * Sends the results to the player after the tick is executed.
     * @param player {@link EnginePlayer}
     * @param absolute Whether absolute coordinates should be sent to the player.
     *                 True at defined intervals.
     */
    void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute);
    
    /**
     * Used for display in {@link EntityTracker}.
     * @param player {@link EnginePlayer}
     */
    void show(EngineEntity engineEntity, EnginePlayer player);
    
    /**
     * Used for display in {@link EntityTracker}.
     * @param player {@link EnginePlayer}
     */
    void hide(EngineEntity engineEntity, EnginePlayer player);

    /**
     * Get an entity instance wrapped in bukkit api.
     * @return Bukkit entity.
     */
    Entity getBukkitEntity();

}

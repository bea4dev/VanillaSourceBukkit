package com.github.bea4dev.vanilla_source.api.nms.entity;

import com.github.bea4dev.vanilla_source.api.entity.controller.EntityController;
import org.bukkit.entity.Entity;

/**
 * NMS entity controller.
 * It is used as an interface for NMS entities.
 * Implementing classes always extend NMS entities.
 */
public interface NMSEntityController extends EntityController {
    
    /**
     * Get bukkit entity instance.
     * Note that most of Bukkit's methods are not thread-safe.
     * @return Bukkit entity
     */
    Entity getBukkitEntity();
    
}

package com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSEntityController;

public class EntityManager {
    
    public static <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        
        switch (type){
            case PLAYER: {
                return new ImplEntityControllerPlayer(
                        MinecraftServer.getServer(),
                        worldServer,
                        (GameProfile) data,
                        ClientInformation.createDefault()
                );
            }
            
            case ITEM: {
                net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy((ItemStack) data);
                return new ImplEntityControllerItem(worldServer, x, y, z, itemStack);
            }

            case ARMOR_STAND: {
                return new ImplEntityControllerArmorStand(worldServer, x, y, z);
            }
        }
        
        throw new IllegalArgumentException("Entity type " + type + " is not supported.");
    }
    
}
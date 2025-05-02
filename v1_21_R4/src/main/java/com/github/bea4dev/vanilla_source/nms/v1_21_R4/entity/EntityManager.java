package com.github.bea4dev.vanilla_source.nms.v1_21_R4.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSEntityController;

public class EntityManager {
    
    public static <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        
        switch (type){
            case PLAYER: {
                var controller = new ImplEntityControllerPlayer(
                        MinecraftServer.getServer(),
                        worldServer,
                        (GameProfile) data,
                        ClientInformation.createDefault()
                );
                controller.setPos(x, y, z);
                return controller;
            }
            
            case ITEM: {
                net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy((ItemStack) data);
                var controller = new ImplEntityControllerItem(worldServer, x, y, z, itemStack);
                controller.setPos(x, y, z);
                return controller;
            }

            case ARMOR_STAND: {
                var controller = new ImplEntityControllerArmorStand(worldServer, x, y, z);
                controller.setPos(x, y, z);
                return controller;
            }

            case ITEM_DISPLAY: {
                var controller = new ImplEntityControllerItemDisplay(worldServer);
                controller.setPos(x, y, z);
                if (data != null) {
                    controller.setItemStack(CraftItemStack.asNMSCopy((ItemStack) data));
                }
                return controller;
            }

            case INTERACTION: {
                var controller = new ImplEntityControllerInteraction(worldServer);
                controller.setPos(x, y, z);
                return controller;
            }

            case BLOCK_DISPLAY: {
                var controller = new ImplEntityControllerBlockDisplay(worldServer);
                controller.setPos(x, y, z);
                return controller;
            }

            case OAK_BOAT: {
                var controller = new ImplEntityControllerBoat(worldServer, x, y, z);
                controller.setPos(x, y, z);
                return controller;
            }
        }
        
        throw new IllegalArgumentException("Entity type " + type + " is not supported.");
    }
    
}

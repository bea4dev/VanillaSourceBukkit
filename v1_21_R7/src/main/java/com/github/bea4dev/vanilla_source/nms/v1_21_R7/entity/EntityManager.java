package com.github.bea4dev.vanilla_source.nms.v1_21_R7.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSEntityController;

import java.util.Set;
import java.util.function.Predicate;

public class EntityManager {
    
    public static <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        ServerLevel worldServer = ((CraftWorld) world).getHandle();

        if (type.name().equals("BOAT") || type.name().endsWith("_BOAT")) {
            var controller = new ImplEntityControllerBoat(worldServer, x, y, z);
            controller.setPos(x, y, z);
            return controller;
        }
        
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

        }
        
        throw new IllegalArgumentException("Entity type " + type + " is not supported.");
    }

    static ServerEntity createServerEntity(ServerLevel world, net.minecraft.world.entity.Entity entity) {
        return new ServerEntity(
                world,
                entity,
                Integer.MAX_VALUE,
                false,
                new ServerEntity.Synchronizer() {
                    @Override
                    public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
                    }

                    @Override
                    public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
                    }

                    @Override
                    public void sendToTrackingPlayersFiltered(
                            Packet<? super ClientGamePacketListener> packet,
                            Predicate<ServerPlayer> filter
                    ) {
                    }

                },
                Set.of()
        );
    }

    static ClientboundTeleportEntityPacket createTeleportPacket(
            net.minecraft.world.entity.Entity entity,
            boolean onGround
    ) {
        return new ClientboundTeleportEntityPacket(
                entity.getId(),
                PositionMoveRotation.of(entity),
                Set.of(),
                onGround
        );
    }
    
}

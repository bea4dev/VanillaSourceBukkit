package com.github.bea4dev.vanilla_source.nms.v1_21_R4.entity.dummy;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class EmptyNetworkManager extends ServerGamePacketListenerImpl {

    public EmptyNetworkManager(
            MinecraftServer minecraftServer,
            Connection networkManager,
            ServerPlayer entityPlayer,
            CommonListenerCookie commonListenerCookie
    ) {
        super(minecraftServer, networkManager, entityPlayer, commonListenerCookie);
    }

    @Override
    public void resumeFlushing() {}

    @Override
    public void send(Packet<?> packet) {}

}
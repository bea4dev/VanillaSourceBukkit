package com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity.dummy;

import com.github.bea4dev.vanilla_source.api.entity.dummy.network.EmptyChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

import java.net.SocketAddress;

public class EmptyPlayerConnection extends Connection {
    
    public EmptyPlayerConnection(PacketFlow packetFlow) {
        super(packetFlow);
        channel = new EmptyChannel(null);
        address = new SocketAddress() {
            private static final long serialVersionUID = 8207338859896320185L;
        };
    }

    @Override
    public void flushChannel() {}

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(Packet packet) {}

    @Override
    public void send(Packet packet, PacketSendListener packetSendListener) {}

    @Override
    public void send(Packet packet, PacketSendListener packetSendListener, boolean flag) {}
    
}


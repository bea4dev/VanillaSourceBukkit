package com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity;

import com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity.dummy.EmptyNetworkManager;
import com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity.dummy.EmptyPlayerConnection;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.*;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.entity.dummy.network.EmptySocket;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSEntityControllerPlayer;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.math.Vec2f;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ImplEntityControllerPlayer extends ServerPlayer implements NMSEntityControllerPlayer {

    private final ServerEntity serverEntity;
    
    public ImplEntityControllerPlayer(
            MinecraftServer minecraftserver,
            ServerLevel serverLevel,
            GameProfile gameprofile,
            ClientInformation clientInformation
    ) {
        super(minecraftserver, serverLevel, gameprofile, clientInformation);
    
        Socket socket = new EmptySocket();
        try {
            EmptyPlayerConnection emptyPlayerConnection = new EmptyPlayerConnection(PacketFlow.CLIENTBOUND);
            connection = new EmptyNetworkManager(
                    minecraftserver,
                    emptyPlayerConnection,
                    this,
                    new CommonListenerCookie(gameprofile, 0, clientInformation, false)
            );
            socket.close();
        } catch (IOException e) {
            //Ignore
        }
        
        //super.valid = false;

        this.serverEntity = new ServerEntity(
                serverLevel,
                this,
                Integer.MAX_VALUE,
                false,
                packet -> {},
                Collections.emptySet()
        );

        // full skin
        var metadata = super.getEntityData();
        metadata.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);
    }
    
    @Override
    public void setPositionRaw(double x, double y, double z) {
        super.setPosRaw(x, y, z);
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        NumberConversions.checkFinite(pitch, "pitch not finite");
        NumberConversions.checkFinite(yaw, "yaw not finite");
        yaw = Location.normalizeYaw(yaw);
        pitch = Location.normalizePitch(pitch);
        super.getBukkitEntity().setRotation(yaw, pitch);
    }
    
    @Override
    public Vector getPosition() {
        return super.getBukkitEntity().getLocation().toVector();
    }
    
    @Override
    public Vec2f getYawPitch() {
        return new Vec2f(super.getBukkitYaw(), super.getXRot());
    }
    
    @Override
    public EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity) {
        AABB aabb = super.getBoundingBox();
        return new EngineEntityBoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, entity);
    }
    
    @Override
    public void resetBoundingBoxForMovement(EngineBoundingBox boundingBox) {
        super.setBoundingBox(new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()));
    }
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
        player.sendPacket(new ClientboundRotateHeadPacket(this, (byte) ((super.getYRot() * 256.0F) / 360.0F)));
        
        if (absolute) {
            player.sendPacket(new ClientboundTeleportEntityPacket(this));
        } else {
            Vector delta = engineEntity.getMoveDelta();
            if (!delta.isZero()) {
                player.sendPacket(new ClientboundMoveEntityPacket.PosRot(
                        super.getId(),
                        (short) (delta.getX() * 4096),
                        (short) (delta.getY() * 4096),
                        (short) (delta.getZ() * 4096),
                        (byte) ((super.getYRot() * 256.0F) / 360.0F),
                        (byte) ((super.getXRot() * 256.0F) / 360.0F),
                        engineEntity.isOnGround()
                ));
            }
        }

        var metadata = super.getEntityData().packDirty();
        if (metadata != null) {
            player.sendPacket(new ClientboundSetEntityDataPacket(super.getId(), metadata));
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this));
        player.sendPacket(new ClientboundAddEntityPacket(this, this.serverEntity));
        player.sendPacket(new ClientboundTeleportEntityPacket(this));
        player.sendPacket(new ClientboundRotateHeadPacket(this, (byte) ((super.getYRot() * 256.0F) / 360.0F)));
        player.sendPacket(new ClientboundAnimatePacket(this, 0));
        var metadata = super.getEntityData().getNonDefaultValues();
        if (metadata != null) {
            player.sendPacket(new ClientboundSetEntityDataPacket(super.getId(), metadata));
        }
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundPlayerInfoRemovePacket(List.of(super.getUUID())));
        player.sendPacket(new ClientboundRemoveEntitiesPacket(super.getId()));
    }
}


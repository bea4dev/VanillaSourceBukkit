package com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity;

import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSEntityController;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.math.Vec2f;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;
import org.bukkit.util.Vector;

import java.util.Collections;

public class ImplEntityControllerBoat extends Boat implements NMSEntityController {
    private final ServerEntity serverEntity;

    private boolean isMetadataChanged = false;

    public ImplEntityControllerBoat(ServerLevel world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
        this.serverEntity = new ServerEntity(
                world,
                this,
                Integer.MAX_VALUE,
                false,
                packet -> {},
                Collections.emptySet()
        );
    }

    @Override
    public void setPositionRaw(double x, double y, double z) {
        super.setPosRaw(x, y, z);
    }

    @Override
    public void setRotation(float yaw, float pitch) {
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

        if (isMetadataChanged) {
            isMetadataChanged = false;
            var dirty = super.getEntityData().packDirty();
            if (dirty != null) {
                player.sendPacket(new ClientboundSetEntityDataPacket(
                        super.getId(),
                        dirty
                ));
            }
        }
    }

    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundAddEntityPacket(this, this.serverEntity));
        player.sendPacket(new ClientboundTeleportEntityPacket(this));
        player.sendPacket(new ClientboundSetEntityDataPacket(super.getId(), this.getEntityData().getNonDefaultValues()));
    }

    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundRemoveEntitiesPacket(super.getId()));
    }

    @Override
    public void setMetadataChanged(boolean is) {
        isMetadataChanged = is;
    }
}

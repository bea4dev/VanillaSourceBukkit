package com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSItemEntityController;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.math.Vec2f;

import java.util.Collections;

public class ImplEntityControllerItem extends ItemEntity implements NMSItemEntityController {

    private final ServerEntity serverEntity;

    public ImplEntityControllerItem(ServerLevel world, double d0, double d1, double d2, net.minecraft.world.item.ItemStack itemStack) {
        super(world, d0, d1, d2, itemStack);
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
        //None
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
        if (absolute) {
            player.sendPacket(new ClientboundTeleportEntityPacket(this));
        } else {
            Vector moveDelta = engineEntity.getMoveDelta();
            if (!moveDelta.isZero()) {
                player.sendPacket(new ClientboundSetEntityMotionPacket(
                        super.getId(),
                        new Vec3(moveDelta.getX(), moveDelta.getY(), moveDelta.getZ())
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
        player.sendPacket(new ClientboundAddEntityPacket(this, this.serverEntity));
        var metadata = super.getEntityData().getNonDefaultValues();
        if (metadata != null) {
            player.sendPacket(new ClientboundSetEntityDataPacket(super.getId(), metadata));
        }
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new ClientboundRemoveEntitiesPacket(super.getId()));
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        super.setItem(CraftItemStack.asNMSCopy(itemStack));
    }
    
}

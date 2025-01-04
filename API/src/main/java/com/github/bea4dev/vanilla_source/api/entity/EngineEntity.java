package com.github.bea4dev.vanilla_source.api.entity;

import com.github.bea4dev.vanilla_source.api.entity.controller.EntityAIController;
import com.github.bea4dev.vanilla_source.api.entity.controller.EntityController;
import com.github.bea4dev.vanilla_source.api.entity.model.ModeledEntityHolder;
import com.github.bea4dev.vanilla_source.api.entity.tick.EntityTracker;
import com.github.bea4dev.vanilla_source.api.entity.tick.TickThread;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.collision.*;
import com.github.bea4dev.vanilla_source.api.world.cache.EngineChunk;
import com.github.bea4dev.vanilla_source.api.world.cache.EngineWorld;
import com.github.bea4dev.vanilla_source.api.world.cache.local.ThreadLocalCache;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.ContanVoidObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.nms.INMSHandler;
import com.github.bea4dev.vanilla_source.api.util.math.Vec2f;
import com.github.bea4dev.vanilla_source.api.world.ChunkUtil;
import com.github.bea4dev.vanilla_source.api.world.EngineLocation;
import com.github.bea4dev.vanilla_source.api.world.block.EngineBlock;

import java.util.*;

public class EngineEntity implements TickBase {

    protected final EntityController entityController;

    protected TickThread tickThread;

    protected ContanClassInstance scriptHandle;

    protected EngineWorld world;
    protected EngineChunk chunk;
    protected double x;
    protected double y;
    protected double z;
    protected float yaw;
    protected float pitch;

    protected double previousX;
    protected double previousY;
    protected double previousZ;

    protected EntityAIController aiController;

    protected Vector velocity = new Vector(0.0, 0.0, 0.0);

    protected boolean collideEntities = false;

    protected boolean onGround = false;

    protected boolean hasGravity = true;

    protected boolean dead = false;

    protected float autoClimbHeight = 0.0F;

    protected CollideOption movementCollideOption = new CollideOption(FluidCollisionMode.NEVER, true);

    public boolean teleported = false;

    protected ModeledEntityHolder modeledEntityHolder = null;

    /**
     * Create entity instance.
     *
     * @param world            World in which this entity exists
     * @param entityController NMS handle
     * @param tickThread       {@link TickThread} that executes the processing of this entity
     * @param scriptHandle     Contan script handle
     */
    public EngineEntity(@NotNull EngineWorld world, @NotNull EntityController entityController, @NotNull TickThread tickThread, @Nullable ContanClassInstance scriptHandle) {
        this.world = world;
        this.entityController = entityController;
        this.tickThread = tickThread;
        this.scriptHandle = scriptHandle;

        //Initialize position and rotation
        Vector position = entityController.getPosition();
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();

        this.previousX = x;
        this.previousY = y;
        this.previousZ = z;

        Vec2f yawPitch = entityController.getYawPitch();
        this.yaw = yawPitch.x;
        this.pitch = yawPitch.y;

        this.chunk = world.getChunkAt(NumberConversions.floor(x) >> 4, NumberConversions.floor(z) >> 4);
        this.aiController = new EntityAIController(this);

        this.setAutoClimbHeight(1.0F);
    }

    /**
     * Gets entity handle.
     *
     * @return {@link EntityController}
     */
    public EntityController getController() {
        return entityController;
    }

    /**
     * Gets the world in which this entity exists.
     *
     * @return {@link EngineWorld}
     */
    public @NotNull EngineWorld getWorld() {
        return world;
    }

    /**
     * Gets the chunk in which this entity exists.
     *
     * @return {@link EngineChunk}
     */
    public @Nullable EngineChunk getChunk() {
        return chunk;
    }

    /**
     * Gets entity location.
     *
     * @return {@link EngineLocation}
     */
    public @NotNull EngineLocation getLocation() {
        return new EngineLocation(world, x, y, z, yaw, pitch);
    }

    /**
     * Gets the height of the block that the entity will automatically climb.
     *
     * @return The height of the block that the entity will automatically climb.
     */
    public float getAutoClimbHeight() {
        return autoClimbHeight;
    }

    /**
     * Sets the height of the block that the entity will automatically climb.
     *
     * @param autoClimbHeight The height of the block that the entity will automatically climb.
     */
    public void setAutoClimbHeight(float autoClimbHeight) {
        this.autoClimbHeight = autoClimbHeight;
        aiController.navigator.setJumpHeight(autoClimbHeight);
    }

    @Override
    public boolean shouldRemove() {
        return dead;
    }

    /**
     * Gets whether this entity is dead or not.
     *
     * @return Whether this entity is dead or not.
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Kill this entity.
     */
    public void kill() {
        dead = true;

        //remove from chunk
        if (chunk != null) {
            int sectionIndex = ChunkUtil.getSectionIndexAligned(NumberConversions.floor(y));
            Set<EngineEntity> entities = chunk.getEntitiesInSection(sectionIndex);

            if (entities.size() != 0) {
                entities.remove(this);
            }
        }
    }

    /**
     * Gets if the entity is standing on the ground.
     *
     * @return Whether the entity is standing on the ground.
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Sets whether this entity performs collision determination with other entities.
     *
     * @param collideEntities Whether this entity performs collision determination with other entities.
     */
    public void setCollideEntities(boolean collideEntities) {
        this.collideEntities = collideEntities;
    }

    /**
     * Gets whether this entity performs collision determination with other entities.
     *
     * @return Whether this entity performs collision determination with other entities.
     */
    public boolean isCollideEntities() {
        return collideEntities;
    }

    /**
     * Gets whether this entity has a BoundingBox.
     *
     * @return Whether this entity has a BoundingBox.
     */
    public boolean hasBoundingBox() {
        return entityController != null;
    }

    /**
     * Gets entity bounding box.
     *
     * @return {@link EngineEntityBoundingBox}
     */
    public @Nullable EngineEntityBoundingBox getBoundingBox() {
        if (entityController == null) return null;
        return entityController.getEngineBoundingBox(this);
    }

    /**
     * Gets whether gravity should be applied to this entity.
     *
     * @return Whether gravity should be applied to this entity.
     */
    public boolean hasGravity() {
        return hasGravity;
    }

    /**
     * Sets whether gravity should be applied to this entity.
     *
     * @param hasGravity Whether gravity should be applied to this entity.
     */
    public void setGravity(boolean hasGravity) {
        this.hasGravity = hasGravity;
    }

    /**
     * Gets an instance of {@link TickThread} executing a tick.
     *
     * @return {@link TickThread}
     */
    public @NotNull TickThread getTickThread() {
        return tickThread;
    }

    /**
     * Sets an instance of {@link TickThread} executing a tick.
     *
     * @param tickThread {@link TickThread}
     */
    public void setTickRunner(TickThread tickThread) {
        this.tickThread = tickThread;
    }

    /**
     * Gets velocity of the entity.
     *
     * @return Velocity of the entity
     */
    public Vector getVelocity() {
        return velocity.clone();
    }

    /**
     * Sets velocity of the entity.
     *
     * @param velocity Velocity of the entity
     */
    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    /**
     * Gets collide option for movement.
     *
     * @return {@link CollideOption}
     */
    public CollideOption getMovementCollideOption() {
        return movementCollideOption;
    }

    /**
     * Sets collide option fot movement.
     *
     * @param movementCollideOption {@link CollideOption}
     */
    public void setMovementCollideOption(CollideOption movementCollideOption) {
        this.movementCollideOption = movementCollideOption;
    }

    /**
     * Gets entity AI and navigation controller.
     *
     * @return {@link EntityAIController}
     */
    public EntityAIController getAIController() {
        return aiController;
    }

    /**
     * Gets Contan script handle
     *
     * @return {@link ContanClassInstance}
     */
    public ContanClassInstance getScriptHandle() {
        return scriptHandle;
    }

    /**
     * Gets entity's position.
     *
     * @return Position vector.
     */
    public Vector getPosition() {
        return new Vector(x, y, z);
    }

    /**
     * Gets entity's rotation.
     *
     * @return Rotation Vec2F.
     */
    public Vec2f getRotation() {
        return new Vec2f(yaw, pitch);
    }

    /**
     * Switch entity's universe.
     *
     * @param universe To universe.
     */
    public void switchUniverse(ParallelUniverse universe) {
        ParallelUniverse previousUniverse = this.getUniverse();
        if (previousUniverse != null) {
            getTrackedPlayers().forEach(this::hide);
        }

        int chunkX = NumberConversions.floor(x) >> 4;
        int chunkZ = NumberConversions.floor(z) >> 4;
        int sectionIndex = ChunkUtil.getSectionIndexAligned(NumberConversions.floor(y));
        EngineChunk chunk = world.getChunkAt(chunkX, chunkZ);
        chunk.getEntitiesInSection(sectionIndex).remove(this);

        if (universe != null) {
            this.world = tickThread.getThreadLocalCache().getParallelWorld(universe, world.getName());
            getTrackedPlayers().forEach(this::show);

            chunk = world.getChunkAt(chunkX, chunkZ);
            chunk.getEntitiesInSection(sectionIndex).add(this);
        }
    }

    /**
     * Gets all player who can see this entity.
     *
     * @return Collection of players who can see this entity.
     */
    public Collection<EnginePlayer> getTrackedPlayers() {
        ParallelUniverse universe = this.getUniverse();
        if (universe == null) {
            return EntityTracker.getPlayersInTrackingRange(x, y, z);
        } else {
            Collection<EnginePlayer> players = EntityTracker.getPlayersInTrackingRange(x, y, z);
            players.removeIf(enginePlayer -> enginePlayer.getUniverse() != universe);
            return players;
        }
    }

    public @Nullable ParallelUniverse getUniverse() {
        if (world instanceof ParallelWorld) {
            return ((ParallelWorld) world).getUniverse();
        }
        return null;
    }

    /**
     * Moves this entity by the specified amount.
     *
     * @param movement Vector to move an entity
     * @return {@link MovementResult}
     */
    public @NotNull MovementResult move(Vector movement) {
        if (!hasBoundingBox()) return MovementResult.EMPTY_MOVEMENT_RESULT;

        if (entityController == null) return MovementResult.EMPTY_MOVEMENT_RESULT;

        EngineBoundingBox originalBoundingBox = getBoundingBox();
        if (originalBoundingBox == null) return MovementResult.EMPTY_MOVEMENT_RESULT;

        if (movement.isZero()) return MovementResult.EMPTY_MOVEMENT_RESULT;

        EngineBoundingBox entityBox = originalBoundingBox.clone().expandForMovement(movement);
        entityBox.expand(movementCollideOption.getBoundingBoxGrow());
        entityBox.expand(1.5);

        //collect collisions
        Set<EngineBoundingBox> boxList = new HashSet<>();

        //get block collisions
        int startX = NumberConversions.floor(entityBox.getMinX());
        int startY = NumberConversions.floor(entityBox.getMinY());
        int startZ = NumberConversions.floor(entityBox.getMinZ());

        int endX = NumberConversions.floor(entityBox.getMaxX());
        int endY = NumberConversions.floor(entityBox.getMaxY());
        int endZ = NumberConversions.floor(entityBox.getMaxZ());

        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();

        EngineChunk chunk = null;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                for (int z = startZ; z < endZ; z++) {
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;

                    //get chunk cache
                    if (chunk == null) {
                        chunk = world.getChunkAt(chunkX, chunkZ);
                    } else if (chunk.getChunkX() != chunkX || chunk.getChunkZ() != chunkZ) {
                        chunk = world.getChunkAt(chunkX, chunkZ);
                    }
                    if (!chunk.isLoaded()) {
                        boxList.add(EngineBoundingBox.getBoundingBoxForUnloadChunk(chunkX, chunkZ));
                        continue;
                    }

                    //get nms block from cache
                    Object iBlockData = chunk.getNMSBlockData(x, y, z);
                    if (iBlockData == null) {
                        continue;
                    }

                    EngineBlock engineBlock = new EngineBlock(world, chunk, x, y, z, iBlockData);

                    //collect block collisions
                    nmsHandler.collectBlockCollisions(engineBlock, boxList, movementCollideOption);
                }
            }
        }

        entityBox = originalBoundingBox;

        //apply collision option
        boxList.removeIf(boundingBox -> {
            if (movementCollideOption.getBoundingBoxCollisionFilter() != null) {
                if (!movementCollideOption.getBoundingBoxCollisionFilter().apply(boundingBox)) {
                    return true;
                }
            }
            if (movementCollideOption.getBlockCollisionFilter() != null) {
                if (boundingBox instanceof EngineBlockBoundingBox) {
                    if (!movementCollideOption.getBlockCollisionFilter().apply(((EngineBlockBoundingBox) boundingBox).getBlock())) {
                        return true;
                    }
                }
            }
            if (movementCollideOption.getEntityCollisionFilter() != null) {
                if (boundingBox instanceof EngineEntityBoundingBox) {
                    return !movementCollideOption.getEntityCollisionFilter().apply(((EngineEntityBoundingBox) boundingBox).getEntity());
                }
            }
            return false;
        });

        //perform movement
        PerformCollisionResult result = entityBox.performCollisions(movement, boxList);
        //get hit collisions for movement result
        List<EngineBoundingBox> hitCollisions = new ArrayList<>(result.getHitCollisions());

        Vector limitedMovement = result.getLimitedMovement();

        //perform auto climb
        if (this.autoClimbHeight > 0.0F && (this.onGround || (limitedMovement.getY() != movement.getY() && movement.getY() < 0.0)) && (limitedMovement.getX() != movement.getX() || limitedMovement.getZ() != movement.getZ())) {
            PerformCollisionResult autoClimbResult = entityBox.performCollisions(new Vector(movement.getX(), this.autoClimbHeight, movement.getZ()), boxList);
            PerformCollisionResult autoClimbUpToResult = ((EngineBoundingBox) entityBox.clone().expand(movement.getX(), 0.0, movement.getZ())).clone().performCollisions(new Vector(0.0, this.autoClimbHeight, 0.0), boxList);

            hitCollisions.addAll(autoClimbResult.getHitCollisions());
            hitCollisions.addAll(autoClimbUpToResult.getHitCollisions());

            Vector autoClimbMovement = autoClimbResult.getLimitedMovement();
            Vector autoClimbUpToMovement = autoClimbUpToResult.getLimitedMovement();

            if (autoClimbUpToMovement.getY() < this.autoClimbHeight) {
                PerformCollisionResult afterClimbResult = ((EngineBoundingBox) entityBox.clone().shift(autoClimbUpToMovement)).performCollisions(new Vector(movement.getX(), 0.0D, movement.getZ()), boxList);

                hitCollisions.addAll(afterClimbResult.getHitCollisions());

                Vector afterClimbMovement = afterClimbResult.getLimitedMovement();

                if (afterClimbMovement.clone().setY(0).lengthSquared() > autoClimbMovement.clone().setY(0).lengthSquared()) {
                    autoClimbMovement = afterClimbMovement;
                }
            }

            if (autoClimbMovement.clone().setY(0).lengthSquared() > limitedMovement.clone().setY(0).lengthSquared()) {
                PerformCollisionResult climbCheckResult = ((EngineBoundingBox) entityBox.clone().shift(autoClimbMovement)).performCollisions(new Vector(0.0D, -autoClimbMovement.getY() + movement.getY(), 0.0D), boxList);

                hitCollisions.addAll(climbCheckResult.getHitCollisions());

                limitedMovement = autoClimbMovement.add(climbCheckResult.getLimitedMovement());
            }
        }

        //reset position by using bounding box
        if (limitedMovement.lengthSquared() > 1.0E-7D) {
            entityController.resetBoundingBoxForMovement((EngineBoundingBox) this.getBoundingBox().shift(limitedMovement));

            EngineBoundingBox boundingBox = getBoundingBox();
            setPosition((boundingBox.getMinX() + boundingBox.getMaxX()) / 2.0D, boundingBox.getMinY(), (boundingBox.getMinZ() + boundingBox.getMaxZ()) / 2.0D);
        }

        if (movement.getY() > 0.0) {
            this.onGround = false;
        } else {
            this.onGround = movement.getY() < limitedMovement.getY();
        }

        return new MovementResult(hitCollisions);
    }

    /**
     * Set position for movement (not for teleport).
     *
     * @param x position x.
     * @param y position y.
     * @param z position z.
     */
    public void setPosition(double x, double y, double z) {
        int previousBlockX = NumberConversions.floor(this.x);
        int previousBlockY = NumberConversions.floor(this.y);
        int previousBlockZ = NumberConversions.floor(this.z);

        int nextBlockX = NumberConversions.floor(x);
        int nextBlockY = NumberConversions.floor(y);
        int nextBlockZ = NumberConversions.floor(z);

        int previousChunkX = previousBlockX >> 4;
        int previousChunkZ = previousBlockZ >> 4;
        int nextChunkX = nextBlockX >> 4;
        int nextChunkZ = nextBlockZ >> 4;

        int previousSectionIndex = ChunkUtil.getSectionIndexAligned(previousBlockY);
        int nextSectionIndex = ChunkUtil.getSectionIndexAligned(nextBlockY);

        //Moving between chunks
        if (!(previousChunkX == nextChunkX && previousChunkZ == nextChunkZ) ||
                previousSectionIndex != nextSectionIndex) {

            if (chunk == null) {
                chunk = world.getChunkAt(previousChunkX, previousChunkZ);
                if (!chunk.isLoaded()) return; //unload chunk teleport cancel
            }
            Set<EngineEntity> previousEntityList = null;
            if (ChunkUtil.isInRangeHeight(previousBlockY)) {
                previousEntityList = chunk.getEntitiesInSection(previousSectionIndex);
            }

            EngineChunk nextChunk;
            if (previousChunkX != nextChunkX || previousChunkZ != nextChunkZ) {
                nextChunk = world.getChunkAt(nextBlockX >> 4, nextBlockZ >> 4);
            } else {
                nextChunk = chunk;
            }
            if (nextChunk == null) return; //unload chunk teleport cancel

            Set<EngineEntity> nextEntityList = null;
            if (ChunkUtil.isInRangeHeight(nextBlockY)) {
                nextEntityList = nextChunk.getEntitiesInSection(nextSectionIndex);
            }

            if (nextEntityList != null) {
                nextEntityList.add(this);
            }
            if (previousEntityList != null) previousEntityList.remove(this);

            chunk = nextChunk;
        }

        this.x = x;
        this.y = y;
        this.z = z;
        entityController.setPositionRaw(x, y, z);
    }


    public void teleport(String worldName, double x, double y, double z, float yaw, float pitch) {
        ThreadLocalCache cache = tickThread.getThreadLocalCache();
        this.world = getUniverse() == null ? cache.getGlobalWorld(worldName) : cache.getParallelWorld(getUniverse(), worldName);
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);

        this.previousX = x;
        this.previousY = y;
        this.previousZ = z;

        this.teleported = true;
    }

    public void teleport(String worldName, double x, double y, double z) {
        this.teleport(worldName, x, y, z, yaw, pitch);
    }


    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.entityController.setRotation(yaw, pitch);
    }

    public void setRotationLookAt(double x, double y, double z) {
        Location loc;
        Entity bukkitEntity = this.entityController.getBukkitEntity();
        if (bukkitEntity instanceof LivingEntity) {
            loc = ((LivingEntity) bukkitEntity).getEyeLocation();
        } else {
            loc = bukkitEntity.getLocation();
        }

        Location temp = new Location(null, 0, 0, 0);
        temp.setDirection(new Vector(x - loc.getX(), y - loc.getY(), z - loc.getZ()));
        this.setRotation(temp.getYaw(), temp.getPitch());
    }

    @Override
    public void tick() {
        invokeScriptFunction("update1");
        invokeScriptFunction("onTick");

        //gravity
        if (hasGravity) velocity.add(new Vector(0.0D, -0.04D, 0.0D));

        move(velocity);

        //Apply block speed factor.
        applySpeedFactor(getBlockSpeedFactor());

        if (onGround) {
            velocity.setY(0);

            //Apply friction factor.
            applySpeedFactor(getBlockFrictionFactor());
        }

        //AI tick
        aiController.tick(x, y, z);

        //Collide with entity
        if (collideEntities) {
            performCollideEntity();
        }

        var modeledEntityHolder = this.modeledEntityHolder;
        if (modeledEntityHolder != null) {
            modeledEntityHolder.tick(this);
        }

        invokeScriptFunction("update2");
    }

    /**
     * Apply speed factor.
     *
     * @param factor Speed factor for xz.
     */
    protected void applySpeedFactor(float factor) {
        Vector xzVec = new Vector(velocity.getX(), 0.0, velocity.getZ());
        xzVec.multiply(factor);
        velocity.setX(xzVec.getX());
        velocity.setZ(xzVec.getZ());
    }

    /**
     * Gets the speed factor of the block on which the entity stands.
     *
     * @return Speed factor of block.
     */
    protected float getBlockSpeedFactor() {
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        return nmsHandler.getBlockSpeedFactor(world, x, y, z);
    }

    /**
     * Gets the friction factor of the block on which the entity stands.
     *
     * @return Friction factor of block.
     */
    protected float getBlockFrictionFactor() {
        BlockData blockData = this.getGroundBlock();
        if (blockData == null) {
            return 0.91F;
        }
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        float factor = nmsHandler.getBlockFrictionFactor(blockData);
        return this.onGround ? factor * 0.91F : 0.91F;
    }

    public BlockData getGroundBlock() {
        int blockX = NumberConversions.floor(x);
        int blockY = NumberConversions.floor(y - 0.5000001D);
        int blockZ = NumberConversions.floor(z);
        return world.getBlockData(blockX, blockY, blockZ);
    }

    /**
     * Performs collisions with entities in contact.
     */
    protected void performCollideEntity() {
        EngineBoundingBox thisBox = this.getBoundingBox();
        if (thisBox == null) {
            return;
        }

        Collection<EngineEntity> entities = chunk.getEntitiesInSection(ChunkUtil.getSectionIndexAligned(NumberConversions.floor(this.y)));

        for (EngineEntity entity : entities) {
            if (entity == this) {
                continue;
            }

            EngineBoundingBox box = entity.getBoundingBox();

            if (box != null) {
                if (box.overlaps(thisBox)) {
                    Vector pos1 = this.getPosition();
                    Vector pos2 = entity.getPosition();
                    Vector direction = pos1.subtract(pos2);
                    direction = new Vector(direction.getX(), 0, direction.getZ());

                    if (direction.lengthSquared() > 0.0) {
                        this.move(direction.normalize().multiply(0.1));
                    }

                    return;
                }
            }
        }
    }

    /**
     * Invoke contan script function.
     *
     * @param functionName
     * @param arguments
     * @return
     */
    protected ContanObject<?> invokeScriptFunction(String functionName, ContanObject<?>... arguments) {
        if (scriptHandle != null) {
            return scriptHandle.invokeFunctionIgnoreNotFound(tickThread, functionName, arguments);
        }

        return ContanVoidObject.INSTANCE;
    }

    public void spawn() {
        tickThread.addEntity(this);
    }

    /**
     * Sets previous position for {@link EntityTracker}
     */
    public void setPreviousPosition() {
        previousX = x;
        previousY = y;
        previousZ = z;
    }

    /**
     * Sends the results to the player after the tick is executed.
     *
     * @param player   {@link EnginePlayer}
     * @param absolute Whether absolute coordinates should be sent to the player.
     *                 True at defined intervals.
     */
    public void playTickResult(EnginePlayer player, boolean absolute) {
        entityController.playTickResult(this, player, absolute || teleported);
        teleported = false;
    }

    /**
     * Used for display in {@link EntityTracker}.
     *
     * @param player {@link EnginePlayer}
     */
    public void show(EnginePlayer player) {
        entityController.show(this, player);

        var modeledEntityHolder = this.modeledEntityHolder;
        if (modeledEntityHolder != null) {
            modeledEntityHolder.show(player);
        }
    }

    /**
     * Used for display in {@link EntityTracker}.
     *
     * @param player {@link EnginePlayer}
     */
    public void hide(EnginePlayer player) {
        entityController.hide(this, player);

        var modeledEntityHolder = this.modeledEntityHolder;
        if (modeledEntityHolder != null) {
            modeledEntityHolder.hide(player);
        }
    }

    /**
     * Gets the amount of movement of an entity.
     *
     * @return Movement delta of {@link Vector}
     */
    public Vector getMoveDelta() {
        return new Vector(x - previousX, y - previousY, z - previousZ);
    }

    public ModeledEntityHolder getModeledEntityHolder() {
        return modeledEntityHolder;
    }

    public void setModeledEntityHolder(ModeledEntityHolder modeledEntityHolder) {
        this.modeledEntityHolder = modeledEntityHolder;
    }
}

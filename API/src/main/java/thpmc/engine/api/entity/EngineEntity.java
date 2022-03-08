package thpmc.engine.api.entity;

import org.bukkit.FluidCollisionMode;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.entity.tick.TickRunner;
import thpmc.engine.api.entity.tick.EntityTracker;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.nms.entity.NMSEntity;
import thpmc.engine.api.player.EnginePlayer;
import thpmc.engine.api.util.collision.CollideOption;
import thpmc.engine.api.util.collision.EngineBoundingBox;
import thpmc.engine.api.util.collision.PerformCollisionResult;
import thpmc.engine.api.util.math.Vec2f;
import thpmc.engine.api.world.ChunkUtil;
import thpmc.engine.api.world.EngineLocation;
import thpmc.engine.api.world.block.EngineBlock;
import thpmc.engine.api.world.cache.EngineChunk;
import thpmc.engine.api.world.cache.EngineWorld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class EngineEntity implements TickBase {
    
    protected final NMSEntity nmsEntity;
    
    protected TickRunner tickRunner;
    
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
    
    protected Vector velocity = new Vector(0.0, 0.0, 0.0);
    
    protected boolean collideEntities = false;
    
    protected boolean onGround = false;
    
    protected boolean hasGravity = true;
    
    protected boolean dead = false;
    
    protected float autoClimbHeight = 0.0F;
    
    protected CollideOption movementCollideOption = new CollideOption(FluidCollisionMode.NEVER, true);
    
    /**
     * Create entity instance.
     * @param world      World in which this entity exists
     * @param nmsEntity  NMS handle
     * @param tickRunner {@link TickRunner} that executes the processing of this entity
     */
    public EngineEntity(@NotNull EngineWorld world, @Nullable NMSEntity nmsEntity, @NotNull TickRunner tickRunner){
        this.world = world;
        this.nmsEntity = nmsEntity;
        this.tickRunner = tickRunner;
        
        //Initialize position and rotation
        if(nmsEntity != null){
            Vector position = nmsEntity.getPosition();
            this.x = position.getX();
            this.y = position.getY();
            this.z = position.getZ();
            
            this.previousX = x;
            this.previousY = y;
            this.previousZ = z;
    
            Vec2f yawPitch = nmsEntity.getYawPitch();
            this.yaw = yawPitch.x;
            this.pitch = yawPitch.y;
        }else{
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
            this.yaw = 0.0F;
            this.pitch = 0.0F;
    
            this.previousX = x;
            this.previousY = y;
            this.previousZ = z;
        }
        
    }
    
    /**
     * Gets a nms handle.
     * @return {@link NMSEntity}
     */
    public @Nullable NMSEntity getHandle() {return nmsEntity;}
    
    /**
     * Gets the world in which this entity exists.
     * @return {@link EngineWorld}
     */
    public @NotNull EngineWorld getWorld() {return world;}
    
    /**
     * Gets the chunk in which this entity exists.
     * @return {@link EngineChunk}
     */
    public @Nullable EngineChunk getChunk() {return chunk;}
    
    /**
     * Gets entity location.
     * @return {@link EngineLocation}
     */
    public @NotNull EngineLocation getLocation() {return new EngineLocation(world, x, y, z, yaw, pitch);}
    
    /**
     * Gets the height of the block that the entity will automatically climb.
     * @return The height of the block that the entity will automatically climb.
     */
    public float getAutoClimbHeight() {return autoClimbHeight;}
    
    /**
     * Sets the height of the block that the entity will automatically climb.
     * @param autoClimbHeight The height of the block that the entity will automatically climb.
     */
    public void setAutoClimbHeight(float autoClimbHeight) {this.autoClimbHeight = autoClimbHeight;}
    
    @Override
    public boolean shouldRemove() {return dead;}
    
    /**
     * Gets whether this entity is dead or not.
     * @return Whether this entity is dead or not.
     */
    public boolean isDead(){return dead;}
    
    /**
     * Gets if the entity is standing on the ground.
     * @return Whether the entity is standing on the ground.
     */
    public boolean isOnGround() {return onGround;}
    
    /**
     * Sets whether this entity performs collision determination with other entities.
     * @param collideEntities Whether this entity performs collision determination with other entities.
     */
    public void setCollideEntities(boolean collideEntities) {this.collideEntities = collideEntities;}
    
    /**
     * Gets whether this entity performs collision determination with other entities.
     * @return Whether this entity performs collision determination with other entities.
     */
    public boolean isCollideEntities() {return collideEntities;}
    
    /**
     * Gets whether this entity has a BoundingBox.
     * @return Whether this entity has a BoundingBox.
     */
    public boolean hasBoundingBox(){return nmsEntity != null;}
    
    /**
     * Gets entity bounding box.
     * @return {@link EngineBoundingBox}
     */
    public @Nullable EngineBoundingBox getBoundingBox(){
        if(nmsEntity == null) return null;
        return nmsEntity.getEngineBoundingBox(this);
    }
    
    /**
     * Gets whether gravity should be applied to this entity.
     * @return Whether gravity should be applied to this entity.
     */
    public boolean hasGravity() {return hasGravity;}
    
    /**
     * Sets whether gravity should be applied to this entity.
     * @param hasGravity Whether gravity should be applied to this entity.
     */
    public void setGravity(boolean hasGravity) {this.hasGravity = hasGravity;}
    
    /**
     * Gets an instance of {@link TickRunner} executing a tick.
     * @return {@link TickRunner}
     */
    public @NotNull TickRunner getTickRunner(){return tickRunner;}
    
    /**
     * Sets an instance of {@link TickRunner} executing a tick.
     * @param tickRunner  {@link TickRunner}
     */
    public void setTickRunner(TickRunner tickRunner){this.tickRunner = tickRunner;}
    
    /**
     * Gets velocity of the entity.
     * @return Velocity of the entity
     */
    public Vector getVelocity() {return velocity.clone();}
    
    /**
     * Sets velocity of the entity.
     * @param velocity Velocity of the entity
     */
    public void setVelocity(Vector velocity) {this.velocity = velocity;}
    
    /**
     * Moves this entity by the specified amount.
     * @param movement Vector to move an entity
     * @return {@link MovementResult}
     */
    public @NotNull MovementResult move(Vector movement){
        if(!hasBoundingBox()) return MovementResult.EMPTY_MOVEMENT_RESULT;
        
        if(nmsEntity == null) return MovementResult.EMPTY_MOVEMENT_RESULT;
        
        EngineBoundingBox originalBoundingBox = getBoundingBox();
        if(originalBoundingBox == null) return MovementResult.EMPTY_MOVEMENT_RESULT;
        
        if(movement.equals(new Vector(0.0, 0.0, 0.0))) return MovementResult.EMPTY_MOVEMENT_RESULT;
        
        double negativeX = Math.abs(Math.min(movement.getX(), 0.0));
        double negativeY = Math.abs(Math.min(movement.getY(), 0.0));
        double negativeZ = Math.abs(Math.min(movement.getZ(), 0.0));
        double positiveX = Math.max(movement.getX(), 0.0);
        double positiveY = Math.max(movement.getY(), 0.0);
        double positiveZ = Math.max(movement.getZ(), 0.0);
        EngineBoundingBox entityBox = (EngineBoundingBox) originalBoundingBox.clone().expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
    
        //collect collisions
        Set<EngineBoundingBox> boxList = new HashSet<>();
    
        //get block collisions
        int startX = NumberConversions.floor(entityBox.getMinX() - 1.5);
        int startY = NumberConversions.floor(entityBox.getMinY() - 1.5);
        int startZ = NumberConversions.floor(entityBox.getMinZ() - 1.5);
    
        int endX = NumberConversions.floor(entityBox.getMaxX() + 1.5);
        int endY = NumberConversions.floor(entityBox.getMaxY() + 1.5);
        int endZ = NumberConversions.floor(entityBox.getMaxZ() + 1.5);
    
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        for(int x = startX; x < endX; x++){
            for(int y = startY; y < endY; y++){
                for(int z = startZ; z < endZ; z++){
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                    
                    //get chunk cache
                    EngineChunk chunk = world.getChunkAt(chunkX, chunkZ);
                    if(chunk == null){
                        boxList.add(EngineBoundingBox.getBoundingBoxForUnloadChunk(chunkX, chunkZ));
                        continue;
                    }
                    
                    //get nms block from cache
                    Object iBlockData = chunk.getNMSBlockData(x, y, z);
                    if(iBlockData == null){
                        continue;
                    }
    
                    EngineBlock engineBlock = new EngineBlock(world, chunk, x, y, z, iBlockData);
                    
                    //collect block collisions
                    nmsHandler.collectBlockCollisions(engineBlock, boxList, movementCollideOption);
                }
            }
        }
    
        entityBox = originalBoundingBox;
    
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
        if(limitedMovement.lengthSquared() > 1.0E-7D){
            nmsEntity.resetBoundingBoxForMovement((EngineBoundingBox) this.getBoundingBox().shift(limitedMovement));
        
            EngineBoundingBox boundingBox = getBoundingBox();
            setPosition((boundingBox.getMinX() + boundingBox.getMaxX()) / 2.0D, boundingBox.getMinY(), (boundingBox.getMinZ() + boundingBox.getMaxZ()) / 2.0D);
        }
    
        if(movement.getY() > 0.0){
            this.onGround = false;
        }else{
            this.onGround = movement.getY() != limitedMovement.getY();
        }
        
        return new MovementResult(hitCollisions);
    }
    
    public void setPosition(double x, double y, double z){
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
        
        int previousSectionIndex = ChunkUtil.getSectionIndex(previousBlockY);
        int nextSectionIndex = ChunkUtil.getSectionIndex(nextBlockY);
        
        //Moving between chunks
        if(!(previousChunkX == nextChunkX && previousChunkZ == nextChunkZ) ||
            previousSectionIndex != nextSectionIndex){
            
            if(chunk == null){
                chunk = world.getChunkAt(previousChunkX, previousChunkZ);
                if(chunk == null) return; //unload chunk teleport cancel
            }
            Set<EngineEntity> previousEntityList = chunk.getEntitiesInSection(previousSectionIndex);
    
            EngineChunk nextChunk;
            if(previousChunkX != nextChunkX || previousChunkZ != nextChunkZ){
                nextChunk = world.getChunkAt(nextBlockX >> 4, nextBlockZ >> 4);
            }else{
                nextChunk = chunk;
            }
            if(nextChunk == null) return; //unload chunk teleport cancel
            
            Set<EngineEntity> nextEntityList = nextChunk.getEntitiesInSection(nextSectionIndex);
            
            nextEntityList.add(this);
            previousEntityList.remove(this);
        }
    
        this.x = x;
        this.y = y;
        this.z = z;
        nmsEntity.setPositionRaw(x, y, z);
    }
    
    public void setRotation(float yaw, float pitch){
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    @Override
    public void tick() {
        //gravity
        if(hasGravity) velocity.add(new Vector(0.0D, -0.04D, 0.0D));
        
        move(velocity);
    
        if(onGround) velocity.setY(0);
    }
    
    /**
     * Sets previous position for {@link EntityTracker}
     */
    public void setPreviousPosition(){
        previousX = x;
        previousY = y;
        previousZ = z;
    }
    
    /**
     * Sends the results to the player after the tick is executed.
     * @param player {@link EnginePlayer}
     * @param absolute Whether absolute coordinates should be sent to the player.
     *                 True at defined intervals.
     */
    public abstract void playTickResult(EnginePlayer player, boolean absolute);
    
    /**
     * Used for display in {@link EntityTracker}.
     * @param player {@link EnginePlayer}
     */
    public abstract void show(EnginePlayer player);
    
    /**
     * Used for display in {@link EntityTracker}.
     * @param player {@link EnginePlayer}
     */
    public abstract void hide(EnginePlayer player);
    
}
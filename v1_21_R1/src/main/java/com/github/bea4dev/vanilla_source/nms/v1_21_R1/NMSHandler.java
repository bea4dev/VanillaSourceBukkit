package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftSound;
import org.bukkit.util.NumberConversions;
import com.github.bea4dev.vanilla_source.api.biome.BiomeDataContainer;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncEngineChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import com.github.bea4dev.vanilla_source.api.nms.INMSHandler;
import com.github.bea4dev.vanilla_source.api.util.BlockPosition3i;
import io.netty.channel.Channel;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import com.github.bea4dev.vanilla_source.api.nms.entity.NMSEntityController;
import com.github.bea4dev.vanilla_source.api.nms.enums.WrappedPlayerInfoAction;
import com.github.bea4dev.vanilla_source.api.util.collision.CollideOption;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBlockBoundingBox;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.world.block.EngineBlock;
import com.github.bea4dev.vanilla_source.api.world.cache.EngineWorld;
import com.github.bea4dev.vanilla_source.nms.v1_21_R1.entity.EntityManager;
import com.github.bea4dev.vanilla_source.nms.v1_21_R1.packet.PacketManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


public class NMSHandler implements INMSHandler {

    private static Field networkManagerField;

    static {
        try {
            networkManagerField = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
            networkManagerField.setAccessible(true);
        } catch (Exception e) { e.printStackTrace(); }
    }


    @Override
    public Channel getChannel(Player player) {
        try {
            ServerGamePacketListenerImpl playerConnection = ((CraftPlayer) player).getHandle().connection;
            Connection connection = (Connection) networkManagerField.get(playerConnection);
            return connection.channel;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().connection.sendPacket((Packet<?>) packet);
    }
    
    @Override
    public Object getNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }
    
    @Override
    public Object getIBlockDataByCombinedId(int id) { return Block.stateById(id); }

    @Override
    public int getCombinedIdByIBlockData(Object iBlockData) { return Block.getId((BlockState) iBlockData); }

    @Override
    public Object getIBlockData(BlockData blockData) { return ((CraftBlockData) blockData).getState(); }

    @Override
    public BlockData getBukkitBlockData(Object iBlockData) { return CraftBlockData.fromData((BlockState) iBlockData); }

    @Override
    public Object[] createIBlockDataArray(int length) { return new BlockState[length]; }

    @Override
    public boolean isMapChunkPacket(Object packet) { return packet instanceof ClientboundLevelChunkWithLightPacket; }

    @Override
    public boolean isMultiBlockChangePacket(Object packet) {
        return packet instanceof ClientboundSectionBlocksUpdatePacket;
    }

    @Override
    public boolean isBlockChangePacket(Object packet) { return packet instanceof ClientboundBlockUpdatePacket; }

    @Override
    public boolean isLightUpdatePacket(Object packet) {
        return packet instanceof ClientboundLightUpdatePacket || packet instanceof ClientboundLevelChunkWithLightPacket;
    }
    
    @Override
    public boolean isFlyPacket(Object packet) { return packet instanceof ServerboundMovePlayerPacket; }
    
    @Override
    public @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ) {
        return PacketManager.createBlockChangePacket(parallelWorld, blockX, blockY, blockZ);
    }
    
    @Override
    public Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks) {
        return PacketManager.createMultiBlockChangePacket(parallelWorld, blocks);
    }
    
    @Override
    public void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk) {
        PacketManager.sendChunkMultiBlockChangeUpdatePacket(player, parallelChunk, this);
    }
    
    @Override
    public @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk) {
        return PacketManager.createLightUpdatePacketAtPrimaryThread(parallelChunk);
    }

    @Override
    public void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk) {
        PacketManager.sendClearChunkMultiBlockChangePacketAtPrimaryThread(player, parallelChunk, this);
    }
    
    @Override
    public <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        return EntityManager.createNMSEntityController(world, x, y, z, type, data);
    }
    
    @Override
    public void collectBlockCollisions(EngineBlock engineBlock, Collection<EngineBoundingBox> boundingBoxCollection, CollideOption collideOption) {
        BlockState iBlockData = ((BlockState) engineBlock.getNMSBlockData());
        List<AABB> alignedBBList;
        
        int blockX = engineBlock.getX();
        int blockY = engineBlock.getY();
        int blockZ = engineBlock.getZ();
        BlockPos blockPosition = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
        
        if (collideOption.isIgnorePassableBlocks()) {
            alignedBBList = iBlockData.getCollisionShape(null, blockPosition).toAabbs();
        } else {
            alignedBBList = iBlockData.getShape(null, blockPosition).toAabbs();
        }

        FluidState fluid = iBlockData.getFluidState();
        if (!fluid.isEmpty()) {
            switch (collideOption.getFluidCollisionMode()) {
                case ALWAYS: {
                    alignedBBList.addAll(getFluidVoxelShape(fluid, engineBlock).toAabbs());
                    break;
                }
                case SOURCE_ONLY: {
                    if (fluid.isSource()) {
                        alignedBBList.addAll(getFluidVoxelShape(fluid, engineBlock).toAabbs());
                    }
                    break;
                }
            }
        }
        
        for (AABB aabb : alignedBBList) {
            boundingBoxCollection.add(new EngineBlockBoundingBox(
                    aabb.minX + blockX,
                    aabb.minY + blockY,
                    aabb.minZ + blockZ,
                    aabb.maxX + blockX,
                    aabb.maxY + blockY,
                    aabb.maxZ + blockZ,
                    engineBlock
            ));
        }
    }
    
    private VoxelShape getFluidVoxelShape(FluidState fluid, EngineBlock block){
        return fluid.getAmount() == 9 && checkUpperBlockHasFluid(fluid, block) ?
                Shapes.block()
                : Shapes.create(0.0D, 0.0D, 0.0D, 1.0D, getFluidHeight(fluid, block), 1.0D);
    }
    
    private float getFluidHeight(FluidState fluid, EngineBlock block){
        return checkUpperBlockHasFluid(fluid, block) ? 1.0F : fluid.getOwnHeight();
    }
    
    private boolean checkUpperBlockHasFluid(FluidState fluid, EngineBlock block){
        EngineWorld world = block.getWorld();
        BlockState upperBlockData = (BlockState) world.getNMSBlockData(block.getX(), block.getY() + 1, block.getZ());
        if(upperBlockData == null) return false;
        
        return fluid.getType().isSame(upperBlockData.getFluidState().getType());
    }

    @Override
    public boolean hasCollision(EngineBlock engineBlock, CollideOption collideOption) {
        BlockState iBlockData = ((BlockState) engineBlock.getNMSBlockData());
        boolean hasCollision = false;

        int blockX = engineBlock.getX();
        int blockY = engineBlock.getY();
        int blockZ = engineBlock.getZ();
        BlockPos blockPosition = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);

        if (collideOption.isIgnorePassableBlocks()) {
            if (!iBlockData.getCollisionShape(null, blockPosition).isEmpty()) {
                hasCollision = true;
            }
        } else {
            if (!iBlockData.getShape(null, blockPosition).isEmpty()) {
                hasCollision = true;
            }
        }

        FluidState fluid = iBlockData.getFluidState();
        if(!fluid.isEmpty()) {
            switch (collideOption.getFluidCollisionMode()) {
                case ALWAYS: {
                    if(!getFluidVoxelShape(fluid, engineBlock).isEmpty()){
                        hasCollision = true;
                    }
                    break;
                }
                case SOURCE_ONLY: {
                    if (fluid.isSource()) {
                        if(!getFluidVoxelShape(fluid, engineBlock).isEmpty()){
                            hasCollision = true;
                        }
                    }
                    break;
                }
            }
        }

        return hasCollision;
    }
    
    @Override
    public void registerBlocksForNative() {
    
    }
    
    @Override
    public void registerChunkForNative(String worldName, AsyncEngineChunk chunk) {
    
    }
    
    @Override
    public float getBlockSpeedFactor(EngineWorld world, double x, double y, double z) {
        int blockX = NumberConversions.floor(x);
        int blockY = NumberConversions.floor(y);
        int blockZ = NumberConversions.floor(z);
    
        BlockState iBlockData = (BlockState) world.getNMSBlockData(blockX, blockY, blockZ);
        if (iBlockData == null) {
            return 1.0F;
        }
    
        Block block = iBlockData.getBlock();
        float factor = block.getSpeedFactor();
        if (block != Blocks.WATER && block != Blocks.BUBBLE_COLUMN) {
            if (factor == 1.0F) {
                int downY = NumberConversions.floor(y - 0.5000001D);
                BlockState halfDown = (BlockState) world.getNMSBlockData(blockX, downY, blockZ);
            
                if (halfDown == null) {
                    return 1.0F;
                }
            
                return halfDown.getBlock().getSpeedFactor();
            } else {
                return factor;
            }
        } else {
            return factor;
        }
    }
    
    @Override
    public float getBlockFrictionFactor(BlockData blockData) {
        BlockState iBlockData = (BlockState) this.getIBlockData(blockData);
        return iBlockData.getBlock().getFriction();
    }
    
    @Override
    public Object getNMSBiomeByKey(String key) {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
        Biome biomeBase;
        Registry<Biome> registryWritable = dedicatedServer.registryAccess().registryOrThrow(Registries.BIOME);
        ResourceKey<Biome> resourceKey = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(key.toLowerCase()));
        biomeBase = registryWritable.get(resourceKey);
        return biomeBase;
    }
    
    @Override
    public void setDefaultBiomeData(BiomeDataContainer container) {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    
        ResourceKey<Biome> oldKey = ResourceKey.create(Registries.BIOME, ResourceLocation.parse("minecraft:forest"));
        Registry<Biome> registryWritable = dedicatedServer.registryAccess().registryOrThrow(Registries.BIOME);
        Biome forestBiome = registryWritable.getOrThrow(oldKey);
        BiomeSpecialEffects specialEffects = forestBiome.getSpecialEffects();

        container.fogColorRGB = specialEffects.getFogColor();
        container.waterColorRGB = specialEffects.getWaterColor();
        container.waterFogColorRGB = specialEffects.getWaterFogColor();
        container.skyColorRGB = specialEffects.getSkyColor();
    }
    
    @Override
    public Object createBiome(String name, BiomeDataContainer container) {
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
        
        ResourceKey<Biome> newKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("custom", name));
    
        ResourceKey<Biome> oldKey = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "forest"));
        Registry<Biome> registryWritable = dedicatedServer.registryAccess().registryOrThrow(Registries.BIOME);
        Biome forestBiome = registryWritable.getOrThrow(oldKey);

        Biome.BiomeBuilder builder = new Biome.BiomeBuilder();
        builder.hasPrecipitation(forestBiome.hasPrecipitation());
        builder.mobSpawnSettings(forestBiome.getMobSettings());
        builder.generationSettings(forestBiome.getGenerationSettings());
        builder.downfall(forestBiome.climateSettings.downfall());

        Float temperature = container.temperature;
        if (temperature != null) {
            builder.temperature(temperature);
        }
        
        switch (container.temperatureAttribute) {
            case NORMAL: {
                builder.temperatureAdjustment(Biome.TemperatureModifier.NONE);
                break;
            }
            case FROZEN: {
                builder.temperatureAdjustment(Biome.TemperatureModifier.FROZEN);
                break;
            }
        }
    
        BiomeSpecialEffects.Builder effectBuilder = new BiomeSpecialEffects.Builder();
        
        switch (container.grassColorAttribute) {
            case NORMAL: {
                effectBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.NONE);
                break;
            }
            case DARK_FOREST: {
                effectBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST);
                break;
            }
            case SWAMP: {
                effectBuilder.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP);
                break;
            }
        }
        
        effectBuilder.fogColor(container.fogColorRGB);
        effectBuilder.waterColor(container.waterColorRGB);
        effectBuilder.waterFogColor(container.waterFogColorRGB);
        effectBuilder.skyColor(container.skyColorRGB);
        
        if (container.foliageColorRGB != null) {
            effectBuilder.foliageColorOverride(container.foliageColorRGB);
        }
        
        if (container.grassBlockColorRGB != null) {
            effectBuilder.grassColorOverride(container.grassBlockColorRGB);
        }
        
        if (container.music != null) {
            //effectBuilder.ambientLoopSound(Holder.direct(CraftSound.bukkitToMinecraft(container.music)));
            effectBuilder.backgroundMusic(
                    new Music(Holder.direct(CraftSound.bukkitToMinecraft(container.music)), 0, 0, true)
            );
        }
        
        if (container.particle != null) {
            Object particleData = container.particleData;
            float particleAmount = container.particleAmount;

            effectBuilder.ambientParticle(new AmbientParticleSettings(
                    CraftParticle.createParticleParam(container.particle, particleData),
                    particleAmount
            ));
        }
    
        builder.specialEffects(effectBuilder.build());

        MappedRegistry<Biome> iRegistryWritable = (MappedRegistry<Biome>) dedicatedServer.registryAccess().registryOrThrow(Registries.BIOME);

        try {
            Field frozen = MappedRegistry.class.getDeclaredField("frozen");
            frozen.setAccessible(true);
            frozen.set(iRegistryWritable, false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Biome biome = builder.build();
        iRegistryWritable.register(newKey, biome, RegistrationInfo.BUILT_IN);

        iRegistryWritable.freeze();
        
        return biome;
    }

    private static void setValueReflection(Object parent, String fieldName, Object value) throws Exception {
        Field field = parent.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.PRIVATE & ~Modifier.FINAL);
        field.set(parent, value);
    }
    
    @Override
    public void setBiomeSettings(String name, BiomeDataContainer container) {
        Biome biomeBase = (Biome) getNMSBiomeByKey("custom:" + name);
        
        try {
    
            Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
            switch (container.temperatureAttribute) {
                case NORMAL: {
                    break;
                }
                case FROZEN: {
                    temperatureModifier = Biome.TemperatureModifier.FROZEN;
                    break;
                }
            }
            setValueReflection(biomeBase.climateSettings, "temperatureModifier", temperatureModifier);
            if (container.temperature != null) {
                setValueReflection(biomeBase.climateSettings, "temperature", container.temperature);
            }
    
            BiomeSpecialEffects effects = biomeBase.getSpecialEffects();
            
            Field b = BiomeFog.class.getDeclaredField("b");
            Field c = BiomeFog.class.getDeclaredField("c");
            Field d1 = BiomeFog.class.getDeclaredField("d");
            Field e = BiomeFog.class.getDeclaredField("e");
            Field f = BiomeFog.class.getDeclaredField("f");
            Field g = BiomeFog.class.getDeclaredField("g");
            Field h = BiomeFog.class.getDeclaredField("h");
            Field i = BiomeFog.class.getDeclaredField("i");
            Field j1 = BiomeFog.class.getDeclaredField("j");
            b.setAccessible(true);
            c.setAccessible(true);
            d1.setAccessible(true);
            e.setAccessible(true);
            f.setAccessible(true);
            g.setAccessible(true);
            h.setAccessible(true);
            i.setAccessible(true);
            j1.setAccessible(true);
    
            BiomeFog.GrassColor grassColor = BiomeFog.GrassColor.a;
            
            switch (container.grassColorAttribute) {
                case NORMAL: {
                    break;
                }
                case DARK_FOREST: {
                    grassColor = BiomeFog.GrassColor.b;
                    break;
                }
                case SWAMP: {
                    grassColor = BiomeFog.GrassColor.c;
                    break;
                }
            }
            h.set(biomeFog, grassColor);
    
            b.setInt(biomeFog, container.fogColorRGB);
            c.setInt(biomeFog, container.waterColorRGB);
            d1.setInt(biomeFog, container.waterFogColorRGB);
            e.setInt(biomeFog, container.skyColorRGB);
    
            if (container.foliageColorRGB != null) {
                f.set(biomeFog, Optional.of(container.foliageColorRGB));
            }
    
            if (container.grassBlockColorRGB != null) {
                g.set(biomeFog, Optional.of(container.grassBlockColorRGB));
            }
    
            if (container.music != null) {
                j1.set(biomeFog, Optional.of(Holder.a(CraftSound.getSoundEffect(container.music))));
            }
    
            if (container.particle != null) {
                Object particleData = container.particleData;
                float particleAmount = container.particleAmount;
        
                if (particleData == null) {
                    i.set(biomeFog, Optional.of(new BiomeParticles(CraftParticle.toNMS(container.particle), particleAmount)));
                } else {
                    i.set(biomeFog, Optional.of(new BiomeParticles(CraftParticle.toNMS(container.particle, particleData), particleAmount)));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    @Override
    public void setBiomeForBlock(org.bukkit.block.Block block, Object biome) {
        Chunk chunk = (Chunk) ((CraftChunk) block.getChunk()).getHandle(ChunkStatus.n);
        Objects.requireNonNull(chunk).setBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2, Holder.a((BiomeBase) biome));
    }
    
    @Override
    public Object createSpawnEntityPacket(Object iEntity) {
        return new PacketPlayOutSpawnEntity((Entity) iEntity);
    }
    
    @Override
    public Object createSpawnEntityLivingPacket(Object iEntityLiving) {
        return new PacketPlayOutSpawnEntity((EntityLiving) iEntityLiving);
    }
    
    @Override
    public Object createMetadataPacket(Object iEntity) {
        Entity entity = (Entity) iEntity;
        return new PacketPlayOutEntityMetadata(entity.af(), entity.aj().c());
    }
    
    @Override
    public Object createPlayerInfoPacket(Object iEntityPlayer, WrappedPlayerInfoAction action) {
        EntityPlayer entityPlayer = (EntityPlayer) iEntityPlayer;

        ClientboundPlayerInfoUpdatePacket.a nmsAction = null;
        switch (action) {
            case ADD_PLAYER:
                nmsAction = ClientboundPlayerInfoUpdatePacket.a.a;
                break;
            case UPDATE_LATENCY:
                nmsAction = ClientboundPlayerInfoUpdatePacket.a.e;
                break;
            case UPDATE_GAME_MODE:
                nmsAction = ClientboundPlayerInfoUpdatePacket.a.c;
                break;
            case UPDATE_DISPLAY_NAME:
                nmsAction = ClientboundPlayerInfoUpdatePacket.a.f;
                break;
            case REMOVE_PLAYER:
                return new ClientboundPlayerInfoRemovePacket(List.of(entityPlayer.ct()));
        }

        return new ClientboundPlayerInfoUpdatePacket(nmsAction, entityPlayer);
    }
    
    @Override
    public Object createSpawnNamedEntityPacket(Object iEntityPlayer) {
        return new PacketPlayOutNamedEntitySpawn((EntityHuman) iEntityPlayer);
    }
    
    @Override
    public Object createTeleportPacket(Object iEntity) {
        return new PacketPlayOutEntityTeleport((Entity) iEntity);
    }
    
    @Override
    public Object createRelEntityMoveLookPacket(Object iEntity, double deltaX, double deltaY, double deltaZ, float yaw, float pitch) {
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(((Entity) iEntity).af(), (short) (deltaX * 4096), (short) (deltaY * 4096), (short) (deltaZ * 4096),
                (byte) ((yaw * 256.0F) / 360.0F), (byte) ((pitch * 256.0F) / 360.0F), true);
    }
    
    @Override
    public Object createHeadRotationPacket(Object iEntity, float yaw) {
        return new PacketPlayOutEntityHeadRotation((Entity) iEntity, (byte) ((yaw * 256.0F) / 360.0F));
    }
    
    @Override
    public Object createEntityDestroyPacket(Object iEntity) {
        return new PacketPlayOutEntityDestroy(((Entity) iEntity).af());
    }
    
    @Override
    public Object createCameraPacket(Object target) {
        return new PacketPlayOutCamera((Entity) target);
    }
    
}

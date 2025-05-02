package com.github.bea4dev.vanilla_source.nms.v1_21_R4;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R4.CraftServer;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.SectionTypeArray;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R4.CraftChunkSnapshot;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;
import com.github.bea4dev.vanilla_source.api.world.cache.AsyncWorldCache;

import java.lang.reflect.Field;

public class MapChunkPacketHandler implements IPacketHandler {
    
    private static Field lightChunkXField;
    private static Field lightChunkZField;
    private static Field lightChunkDataField;
    
    private static Field blockEntitiesDataField;

    private static Field blockids;
    private static Field biome;

    private static Field emptyBlockIDs;

    static {
        try {
            lightChunkXField = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("x");
            lightChunkZField = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("z");
            lightChunkDataField = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("chunkData");
            NMSHandler.setRewritable(lightChunkXField);
            NMSHandler.setRewritable(lightChunkZField);
            NMSHandler.setRewritable(lightChunkDataField);

            blockEntitiesDataField = ClientboundLevelChunkPacketData.class.getDeclaredField("blockEntitiesData");
            NMSHandler.setRewritable(blockEntitiesDataField);

            blockids = CraftChunkSnapshot.class.getDeclaredField("blockids");
            biome = CraftChunkSnapshot.class.getDeclaredField("biome");
            blockids.setAccessible(true);
            biome.setAccessible(true);

            emptyBlockIDs = CraftChunk.class.getDeclaredField("emptyBlockIDs");
            emptyBlockIDs.setAccessible(true);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = enginePlayer.getUniverse();

        World world = enginePlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
    
        DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();

        try {
            Object packetData = lightChunkDataField.get(packet);

            int chunkX = lightChunkXField.getInt(packet);
            int chunkZ = lightChunkZField.getInt(packet);

            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockDifferenceData()) return packet;
    
            Object cachedPacketData = parallelChunk.getCachedMapChunkPacket();
            if (cachedPacketData != null){
                lightChunkDataField.set(packet, cachedPacketData);
                return packet;
            }

            ChunkSnapshot chunkSnapshot = AsyncWorldCache.getChunkCache(worldName, chunkX, chunkZ);
            if (chunkSnapshot == null) return packet;

            PalettedContainer<BlockState>[] cachedDataBlocks = (PalettedContainer<BlockState>[]) blockids.get(chunkSnapshot);
            PalettedContainerRO<Holder<Biome>>[] cachedBiomePalettes = (PalettedContainerRO<Holder<Biome>>[]) biome.get(chunkSnapshot);
            
            int sectionCount = (world.getMaxHeight() - world.getMinHeight()) >> 4;
            int minSection = world.getMinHeight() >> 4;
            
            LevelChunkSection[] chunkSections = new LevelChunkSection[sectionCount];
            boolean edited = false;
            
            for (int index = 0; index < sectionCount; index++) {
                int sectionY = minSection + index;

                LevelChunkSection chunkSection = null;

                SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionY);
                if (sectionTypeArray != null) {
                    PalettedContainer<BlockState> cachedBlockData = cachedDataBlocks[index];
                    PalettedContainerRO<Holder<Biome>> cachedBiomePalette = cachedBiomePalettes[index];

                    if (cachedBlockData != null) {
                        PalettedContainer<BlockState> blocks = cachedBlockData.copy();
                        PalettedContainer<Holder<Biome>> biomes = cachedBiomePalette.recreate();
                        chunkSection = new LevelChunkSection(blocks, biomes);
                    }

                    if (chunkSection == null) {
                        Registry<Biome> biomeRegistry = dedicatedServer.registryAccess().lookupOrThrow(Registries.BIOME);
                        chunkSection = new LevelChunkSection(biomeRegistry);
                    }
    
                    LevelChunkSection finalChunkSection = chunkSection;
                    boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, BlockState) -> {
                        finalChunkSection.setBlockState(x, y, z, (BlockState) BlockState, false);
                    });
                    
                    if (notEmpty) edited = true;

                } else {
                    if (!chunkSnapshot.isSectionEmpty(index)) {
                        PalettedContainer<BlockState> blocks = cachedDataBlocks[index].copy();
                        PalettedContainer<Holder<Biome>> biomes = cachedBiomePalettes[index].recreate();
                        chunkSection = new LevelChunkSection(blocks, biomes);
                    }
                }
                
                chunkSections[index] = chunkSection;
            }

            if (!edited) {
                return packet;
            }

            LevelChunk chunk = new LevelChunk(
                    ((CraftWorld) world).getHandle(),
                    new ChunkPos(chunkX, chunkZ),
                    UpgradeData.EMPTY,
                    new LevelChunkTicks<>(),
                    new LevelChunkTicks<>(),
                    0L,
                    chunkSections,
                    null,
                    null
            );

            ClientboundLevelChunkPacketData newPacketData = new ClientboundLevelChunkPacketData(chunk);

            Object blockEntitiesData = blockEntitiesDataField.get(packetData);
            blockEntitiesDataField.set(newPacketData, blockEntitiesData);
    
            if (cacheSetting) {
                parallelChunk.setMapChunkPacketCache(newPacketData);
            }

            lightChunkDataField.set(packet, newPacketData);
            return packet;

        } catch (Exception e) { e.printStackTrace(); }

        return packet;
    }
}

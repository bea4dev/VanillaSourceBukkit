package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import net.minecraft.core.MappedRegistry;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftChunkSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReflectionFieldTest {

    @Test
    void reflectionFieldNamesMatchPaper() {
        assertFields(ClientboundLevelChunkWithLightPacket.class, "x", "z", "chunkData", "lightData");
        assertFields(ClientboundLevelChunkPacketData.class, "blockEntitiesData");
        assertFields(CraftChunkSnapshot.class, "blockids", "biome");
        assertFields(CraftChunk.class, "emptyBlockIDs");
        assertFields(ClientboundSectionBlocksUpdatePacket.class, "sectionPos", "positions", "states");
        assertFields(ClientboundLightUpdatePacket.class, "lightData");
        assertFields(
                ClientboundLightUpdatePacketData.class,
                "skyYMask",
                "blockYMask",
                "emptySkyYMask",
                "emptyBlockYMask",
                "skyUpdates",
                "blockUpdates"
        );
        assertFields(ServerCommonPacketListenerImpl.class, "connection");
        assertFields(ClientboundSetPassengersPacket.class, "passengers");
        assertFields(MappedRegistry.class, "frozen");
        assertFields(
                BiomeSpecialEffects.class,
                "grassColorModifier",
                "fogColor",
                "waterColor",
                "waterFogColor",
                "skyColor",
                "foliageColorOverride",
                "grassColorOverride",
                "backgroundMusic",
                "ambientParticleSettings"
        );
        assertFields(Level.class, "dimensionTypeRegistration");
        assertFields(ClientboundTeleportEntityPacket.class, "x", "y", "z");
    }

    private static void assertFields(Class<?> type, String... names) {
        for (String name : names) {
            assertDoesNotThrow(
                    () -> type.getDeclaredField(name),
                    () -> type.getName() + " no longer declares field " + name
            );
        }
    }
}

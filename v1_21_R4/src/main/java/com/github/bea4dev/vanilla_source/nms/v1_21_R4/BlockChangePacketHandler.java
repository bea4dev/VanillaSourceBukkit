package com.github.bea4dev.vanilla_source.nms.v1_21_R4;

import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R4.block.data.CraftBlockData;

public class BlockChangePacketHandler implements IPacketHandler {

    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {

        ParallelUniverse universe = EnginePlayer.getUniverse();
    
        String worldName = EnginePlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
            var updatePacket = (ClientboundBlockUpdatePacket) packet;
            var position = updatePacket.getPos();

            int x = position.getX();
            int y = position.getY();
            int z = position.getZ();
    
            ParallelChunk chunk = parallelWorld.getChunk(x >> 4, z >> 4);
            if (chunk == null) {
                return packet;
            }
    
            if (!chunk.hasBlockDataDifference(x, y, z)) {
                return packet;
            }
    
            BlockData blockData = chunk.getBlockData(x, y, z);
            if(blockData == null) return packet;
    
            return new ClientboundBlockUpdatePacket(position, ((CraftBlockData) blockData).getState());
        } catch (Exception e) { e.printStackTrace(); }

        return packet;
    }
}

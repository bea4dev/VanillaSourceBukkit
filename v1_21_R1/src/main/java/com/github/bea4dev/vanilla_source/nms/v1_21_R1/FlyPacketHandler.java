package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;


public class FlyPacketHandler implements IPacketHandler {
    
    /*
    private static Field C;
    private static Field E;
    
    static {
        try {
            C = PlayerConnection.class.getDeclaredField("C");
            E = PlayerConnection.class.getDeclaredField("E");
            
            C.setAccessible(true);
            E.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/
    
    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {
        /*
        ParallelUniverse universe = EnginePlayer.getUniverse();
        if(universe == null) return packet;
        
        World world = EnginePlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
        
        EntityPlayer entityPlayer = ((CraftPlayer) EnginePlayer.getBukkitPlayer()).getHandle();
        
        int x = NumberConversions.floor(entityPlayer.locX());
        int y = NumberConversions.floor(entityPlayer.locY());
        int z = NumberConversions.floor(entityPlayer.locZ());
        
        int downY = y - 1;
        downY = Math.max(0, downY);
    
        if(parallelWorld.hasBlockData(x, y, z) || parallelWorld.hasBlockData(x, downY, z)){
            try {
                PlayerConnection playerConnection = entityPlayer.playerConnection;
                C.set(playerConnection, 0);
                E.set(playerConnection, 0);
            }catch (Exception e){e.printStackTrace();}
        }*/
        
        return packet;
    }
    
}


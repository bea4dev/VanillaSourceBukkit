package com.github.bea4dev.vanilla_source.listener;

import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
    
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to != null) {
            if (from.getWorld() != to.getWorld()) {
                EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer(player);
                
            }
        }
    }
    
}

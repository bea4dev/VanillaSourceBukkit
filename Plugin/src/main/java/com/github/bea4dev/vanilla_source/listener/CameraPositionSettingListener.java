package com.github.bea4dev.vanilla_source.listener;

import com.github.bea4dev.vanilla_source.camera.CameraEditor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CameraPositionSettingListener implements Listener {
    
    @EventHandler
    public void onClickTripwireHook(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (player.getInventory().getItemInMainHand().equals(CameraEditor.setter)) {
            CameraEditor.onSet(player);
        }
    }
    
}

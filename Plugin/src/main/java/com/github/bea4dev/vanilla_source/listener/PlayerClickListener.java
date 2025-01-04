package com.github.bea4dev.vanilla_source.listener;

import com.github.bea4dev.vanilla_source.api.asset.WorldAssetsRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerClickListener implements Listener {
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var action = event.getAction();

        if (!player.getWorld().getName().equals(WorldAssetsRegistry.ASSETS_WORLD_NAME)) {
            return;
        }

        switch (action) {
            case RIGHT_CLICK_AIR -> WorldAssetsRegistry.raytrace(player, 128.0);
            case RIGHT_CLICK_BLOCK -> {
                if (player.getInventory().getItemInMainHand().isEmpty()) {
                    WorldAssetsRegistry.raytrace(player, 3.0);
                }
            }
        }
    }
}

package com.github.bea4dev.vanilla_source.api.text;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.entity.TickBase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextBoxManager implements Listener, TickBase {
    private static final Map<Player, TextBox> textBoxes = new ConcurrentHashMap<>();
    private static final Map<Player, Boolean> sneakMap = new ConcurrentHashMap<>();

    public static void init(Plugin plugin) {
        var instance = new TextBoxManager();
        VanillaSourceAPI.getInstance().getTickThreadPool().getNextTickThread().addEntity(instance);

        var pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(instance, plugin);
    }

    public static void onSneak(Player player) {
        var textBox = textBoxes.get(player);
        if (textBox != null) {
            textBox.next();
        }
    }

    public static void onVehicleSneak(Player player, boolean pressDown) {
        var isPressedDownPrev = sneakMap.get(player);

        if (pressDown) {
            if (isPressedDownPrev == null) {
                onSneak(player);
            } else {
                if (!isPressedDownPrev) {
                    onSneak(player);
                }
            }
        }

        sneakMap.put(player, pressDown);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        var player = event.getPlayer();
        if (event.isSneaking()) {
            onSneak(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        textBoxes.remove(player);
        sneakMap.remove(player);
    }

    static void registerTextBox(Player player, TextBox textBox) {
        textBoxes.put(player, textBox);
    }

    static void unregisterTextBox(Player player) {
        textBoxes.remove(player);
    }

    @Override
    public void tick() {
        for (var textBox : textBoxes.values()) {
            textBox.tick();
        }
    }

    @Override
    public boolean shouldRemove() {
        return false;
    }
}

package com.github.bea4dev.vanilla_source.api.util.gui;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface TextInputListener {
    
    String onInput(Player player, String text);
    
}

package com.github.bea4dev.vanilla_source.command;

import com.github.bea4dev.vanilla_source.biome.BiomeManager;
import com.github.bea4dev.vanilla_source.biome.gui.BiomeGUI;
import com.github.bea4dev.vanilla_source.lang.SystemLanguage;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;

public class BiomeCommand {
    public static void register() {

        new CommandAPICommand("biome").withSubcommands(
                        new CommandAPICommand("menu")
                                .executesPlayer((sender, args) -> {
                                    BiomeGUI.openBiomeSelectGUI(sender, "", biomeSource -> {});
                                }),

                        new CommandAPICommand("set")
                                .executesPlayer((sender, args) -> {
                                    if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
                                        sender.sendMessage(SystemLanguage.getText("world-edit-is-required"));
                                        return;
                                    }
                                    BiomeManager.setBiome(sender);
                                })
                )
                .withPermission("vanilla_source.biome")
                .register();
    }
}

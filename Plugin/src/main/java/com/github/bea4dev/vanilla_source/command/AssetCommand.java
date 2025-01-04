package com.github.bea4dev.vanilla_source.command;

import com.github.bea4dev.vanilla_source.api.asset.WorldAsset;
import com.github.bea4dev.vanilla_source.api.asset.WorldAssetsRegistry;
import com.github.bea4dev.vanilla_source.world_edit.WorldEditUtil;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class AssetCommand {
    public static void register() {
        new CommandAPICommand("asset").withSubcommands(
                new CommandAPICommand("create")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((sender, args) -> {
                            var assetName = (String) args.get(0);

                            if (WorldAssetsRegistry.getAsset(assetName) != null) {
                                sender.sendMessage(
                                        Component.text("'" + assetName + "' already exists!")
                                                .color(NamedTextColor.RED)
                                );
                                return;
                            }

                            var region = WorldEditUtil.getSelectedRegion(sender);
                            if (region == null) {
                                return;
                            }

                            var startPosition = region.getMinimum();
                            var endPosition = region.getMaximum();

                            var worldAsset = new WorldAsset(assetName, startPosition, endPosition);
                            worldAsset.setChanged(true);
                            WorldAssetsRegistry.registerAsset(assetName, worldAsset);
                            worldAsset.load();

                            sender.sendMessage(
                                    Component.text("'" + assetName + "' is created!")
                                            .color(NamedTextColor.GREEN)
                            );
                        }),
                new CommandAPICommand("remove")
                        .withArguments(new StringArgument("name")
                                .replaceSuggestions(ArgumentSuggestions.strings(WorldAssetsRegistry.getAllAssetNames()))
                        )
                        .executesPlayer((sender, args) -> {
                            var assetName = (String) args.get(0);
                            var result = WorldAssetsRegistry.unregisterAsset(assetName);

                            if (result) {
                                sender.sendMessage(
                                        Component.text("'" + assetName + "' is removed!")
                                                .color(NamedTextColor.AQUA)
                                );
                            } else {
                                sender.sendMessage(
                                        Component.text("'" + assetName + "' is not found!")
                                                .color(NamedTextColor.RED)
                                );
                            }
                        }),
                new CommandAPICommand("teleport")
                        .executesPlayer((sender, args) -> {
                            var assetWorld = Bukkit.getWorld(WorldAssetsRegistry.ASSETS_WORLD_NAME);
                            sender.teleport(new Location(assetWorld, 0.5, 1, 0.5));
                        })
        ).executesPlayer((sender, args) -> {
            var assetWorld = Bukkit.getWorld(WorldAssetsRegistry.ASSETS_WORLD_NAME);
            sender.teleport(new Location(assetWorld, 0.5, 1, 0.5));
        }).withPermission("vanilla_source.asset").register();
    }
}

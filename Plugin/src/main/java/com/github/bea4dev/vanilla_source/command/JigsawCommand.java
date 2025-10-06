package com.github.bea4dev.vanilla_source.command;

import com.github.bea4dev.vanilla_source.api.asset.JigsawProcessor;
import com.github.bea4dev.vanilla_source.api.asset.JigsawReferenceManager;
import com.github.bea4dev.vanilla_source.api.asset.WorldAssetsRegistry;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

public class JigsawCommand {
    private static int tempWorldCount = 0;

    public static void register() {
        new CommandAPICommand("jigsaw")
                .withSubcommand(
                        new CommandAPICommand("test")
                                .withArguments(
                                        new NamespacedKeyArgument("start_jigsaw").replaceSuggestions(
                                                ArgumentSuggestions
                                                        .strings((info) ->
                                                                JigsawReferenceManager
                                                                        .getJigsawNames()
                                                                        .stream()
                                                                        .map(NamespacedKey::toString)
                                                                        .toList()
                                                                        .toArray(new String[]{})
                                                        )
                                        )
                                )
                                .executesPlayer((player, args) -> {
                                    var jigsawName = args.get(0);
                                    if (jigsawName == null) {
                                        return;
                                    }

                                    var jigsaws = JigsawReferenceManager.getFromName((NamespacedKey) jigsawName);

                                    if (jigsaws.isEmpty()) {
                                        player.sendMessage(Component.text("No jigsaw found.").color(NamedTextColor.RED));
                                        return;
                                    }

                                    var startJigsaw = jigsaws.getFirst();

                                    var world = Bukkit.createWorld(
                                            WorldCreator
                                                    .name("jigsaw_temp_" + tempWorldCount)
                                                    .generator(new ChunkGenerator() {
                                                    }));
                                    tempWorldCount++;

                                    if (world == null) {
                                        player.sendMessage(Component.text("Failed to create world.").color(NamedTextColor.RED));
                                        return;
                                    }

                                    var startBlock = world.getBlockAt(0, 0, 0);

                                    var jigsawProcessor = new JigsawProcessor(startBlock, startJigsaw, 100);
                                    jigsawProcessor.start();

                                    player.teleport(startBlock.getLocation().add(0.5, 1.0, 0.5));
                                })
                )
                .withSubcommand(new CommandAPICommand("reload")
                        .executesPlayer((player, args) -> {
                            JigsawReferenceManager.clear();
                            WorldAssetsRegistry.reload();
                        }))
                .register();
    }
}

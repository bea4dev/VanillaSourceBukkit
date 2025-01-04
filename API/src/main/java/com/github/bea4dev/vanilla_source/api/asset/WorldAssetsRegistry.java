package com.github.bea4dev.vanilla_source.api.asset;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.entity.tick.TickThread;
import com.github.bea4dev.vanilla_source.api.util.collision.CollideOption;
import com.github.bea4dev.vanilla_source.api.util.collision.EngineBoundingBox;
import com.github.bea4dev.vanilla_source.api.world.cache.EngineWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WorldAssetsRegistry {
    public static final String ASSETS_WORLD_NAME = "vanilla_source_assets_world";

    static TickThread thread;
    private static final Map<String, WorldAsset> assetsMap = new HashMap<>();

    public static WorldAsset getAsset(String name) {
        return assetsMap.get(name);
    }

    public static void registerAsset(String name, WorldAsset asset) {
        Bukkit.getScheduler().runTask(VanillaSourceAPI.getInstance().getPlugin(), asset::createEntity);
        assetsMap.put(name, asset);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean unregisterAsset(String name) {
        var file = new File("plugins/VanillaSource/assets/" + name + ".yml");
        if (file.exists()) {
            file.delete();
        }

        var asset = assetsMap.remove(name);
        if (asset != null) {
            if (asset.entity != null) {
                asset.entity.kill();
                return true;
            }
        }

        return false;
    }

    public static Collection<WorldAsset> getAllAssets() {
        return assetsMap.values();
    }

    public static Collection<String> getAllAssetNames() {
        return assetsMap.keySet();
    }

    public static void raytrace(Player player, double distance) {
        var start = player.getEyeLocation().toVector();
        var direction = player.getEyeLocation().getDirection();

        var collisions = assetsMap.values()
                .stream()
                .map(asset -> asset.entity)
                .map(EngineEntity::getBoundingBox)
                .map(box -> (EngineBoundingBox) box)
                .toList();

        var result = EngineWorld.rayTraceForCollisionList(
                start,
                direction,
                distance,
                collisions,
                new CollideOption(FluidCollisionMode.NEVER, true)
        );

        if (result == null) {
            return;
        }

        var asset = (WorldAssetDisplayEntity) result.getHitEntity();
        assert asset != null;

        player.sendMessage(
                Component.text("This is '")
                        .append(Component.text(asset.asset.getAssetName()).color(NamedTextColor.AQUA))
                        .append(Component.text("'.").color(NamedTextColor.WHITE))
        );
    }

    @SuppressWarnings("all")
    public static void init() {
        thread = VanillaSourceAPI.getInstance().getTickThreadPool().getNextTickThread();

        Bukkit.getScheduler().runTask(VanillaSourceAPI.getInstance().getPlugin(), () -> {
            var creator = new WorldCreator(ASSETS_WORLD_NAME);
            creator.generator(new AssetsWorldChunkGenerator());

            var world = Bukkit.createWorld(creator);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        });

        File dir = new File("plugins/VanillaSource/assets");

        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IllegalStateException("File IO error.");
        }

        for (File file : files) {
            if (file.getName().endsWith(".yml")) {
                VanillaSourceAPI.getInstance().getPlugin().getLogger().info("Loding asset : " + file.getName());
                var worldAsset = WorldAsset.load(file);
                assetsMap.put(file.getName().replace(".yml", ""), worldAsset);

                Bukkit.getScheduler().runTask(VanillaSourceAPI.getInstance().getPlugin(), () -> {
                    worldAsset.load();
                    worldAsset.createEntity();
                });
            }
        }
    }

    public static void save() {
        for (var asset : assetsMap.values()) {
            asset.save();
        }
    }
}

package com.github.bea4dev.vanilla_source.api.asset;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorldAsset {
    private final String assetName;
    private final Vector startPosition;
    private final Vector endPosition;

    private boolean isChanged = false;

    private final Map<Vector, BlockData> blockMap = new HashMap<>();
    private final Map<Vector, BlockState> stateMap = new HashMap<>();

    WorldAssetDisplayEntity entity;

    public WorldAsset(String assetName, Vector startPosition, Vector endPosition) {
        this.assetName = assetName;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    void createEntity() {
        var nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();

        entity = new WorldAssetDisplayEntity(
                this,
                WorldAssetsRegistry.thread.getThreadLocalCache().getGlobalWorld(WorldAssetsRegistry.ASSETS_WORLD_NAME),
                nmsHandler.createNMSEntityController(
                        Bukkit.getWorld(WorldAssetsRegistry.ASSETS_WORLD_NAME),
                        startPosition.getX(),
                        startPosition.getY(),
                        startPosition.getZ(),
                        EntityType.BLOCK_DISPLAY,
                        null
                ),
                WorldAssetsRegistry.thread,
                null
        );
        entity.setPosition(startPosition.getX(), startPosition.getY(), startPosition.getZ());
        entity.setGravity(false);

        var bukkitEntity = (BlockDisplay) entity.getController().getBukkitEntity();
        var transformation = bukkitEntity.getTransformation();
        var scale = endPosition.clone().subtract(startPosition).add(new Vector(1, 1, 1));
        transformation.getScale().set(scale.getX(), scale.getY(), scale.getZ());
        bukkitEntity.setTransformation(transformation);

        bukkitEntity.setGlowing(true);

        bukkitEntity.setBlock(Material.GLASS.createBlockData());

        entity.spawn();
    }

    public void load() {
        var world = Bukkit.getWorld(WorldAssetsRegistry.ASSETS_WORLD_NAME);
        assert world != null;

        var nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();

        for (var x = startPosition.getBlockX(); x <= endPosition.getBlockX(); x++) {
            for (var y = startPosition.getBlockY(); y <= endPosition.getBlockY(); y++) {
                for (var z = startPosition.getBlockZ(); z <= endPosition.getBlockZ(); z++) {
                    var block = world.getBlockAt(x, y, z);
                    var position = new Vector(
                            x - startPosition.getBlockX(),
                            y - startPosition.getBlockY(),
                            z - startPosition.getBlockZ()
                    );
                    blockMap.put(position, block.getBlockData());
                    stateMap.put(position, block.getState());

                    if (block.getType() == Material.JIGSAW) {
                        var jigsawState = nmsHandler.getJigsawState(block);

                        if (jigsawState != null) {
                            var jigsawReference = new JigsawReference(this, position, jigsawState);
                            JigsawReferenceManager.registerReference(jigsawReference);
                        }
                    }
                }
            }
        }
    }

    public void reload() {
        blockMap.clear();
        stateMap.clear();
        load();
    }

    public String getAssetName() {
        return assetName;
    }

    public Vector getStartPosition() {
        return startPosition.clone();
    }

    public Vector getEndPosition() {
        return endPosition.clone();
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public @Nullable BlockData getBlock(int x, int y, int z) {
        return blockMap.get(new Vector(x, y, z));
    }

    public @Nullable BlockState getBlockState(int x, int y, int z) {
        return stateMap.get(new Vector(x, y, z));
    }

    public void place(Vector position, AssetPlacer placer) {
        for (var x = startPosition.getBlockX(); x <= endPosition.getBlockX(); x++) {
            for (var y = startPosition.getBlockY(); y <= endPosition.getBlockY(); y++) {
                for (var z = startPosition.getBlockZ(); z <= endPosition.getBlockZ(); z++) {
                    var addPosition = new Vector(
                            x - startPosition.getBlockX(),
                            y - startPosition.getBlockY(),
                            z - startPosition.getBlockZ()
                    );
                    var blockData = blockMap.get(addPosition);
                    var blockState = stateMap.get(addPosition);

                    var placePosition = position.clone().add(addPosition);

                    placer.place(
                            placePosition.getBlockX(),
                            placePosition.getBlockY(),
                            placePosition.getBlockZ(),
                            blockData,
                            blockState
                    );
                }
            }
        }
    }

    public static WorldAsset load(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        var assetName = file.getName().replace(".yml", "");
        var startPosition = Objects.requireNonNull(yml.getVector("start_position"));
        var endPosition = Objects.requireNonNull(yml.getVector("end_position"));

        return new WorldAsset(assetName, startPosition, endPosition);
    }

    public void save() {
        if (!isChanged) {
            return;
        }

        YamlConfiguration yml = new YamlConfiguration();

        yml.set("start_position", startPosition);
        yml.set("end_position", endPosition);

        File file = new File("plugins/VanillaSource/assets/" + assetName + ".yml");
        try {
            yml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface AssetPlacer {
        void place(int x, int y, int z, @NotNull BlockData blockData, @NotNull BlockState blockState);
    }
}

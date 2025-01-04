package com.github.bea4dev.vanilla_source;

import be4rjp.artgui.ArtGUI;
import com.github.bea4dev.vanilla_source.api.asset.WorldAssetsRegistry;
import com.github.bea4dev.vanilla_source.api.biome.BiomeStore;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.entity.tick.MainThreadTimer;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.setting.VSSettings;
import com.github.bea4dev.vanilla_source.api.text.TextBoxManager;
import com.github.bea4dev.vanilla_source.camera.CameraFileManager;
import com.github.bea4dev.vanilla_source.command.CommandRegistry;
import com.github.bea4dev.vanilla_source.command.HoverTextCommandExecutor;
import com.github.bea4dev.vanilla_source.config.ImplVSSettings;
import com.github.bea4dev.vanilla_source.contan.ContanManager;
import com.github.bea4dev.vanilla_source.lang.SystemLanguage;
import com.github.bea4dev.vanilla_source.listener.*;
import com.github.bea4dev.vanilla_source.command.ParallelCommandExecutor;
import com.github.bea4dev.vanilla_source.util.TaskHandler;
import com.github.bea4dev.vanilla_source.impl.ImplVanillaSourceAPI;
import com.github.bea4dev.vanilla_source.nms.NMSManager;
import com.github.bea4dev.vanilla_source.structure.ParallelStructure;
import com.github.bea4dev.vanilla_source.structure.ImplStructureData;
import com.ticxo.modelengine.api.ModelEngineAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


public final class VanillaSource extends JavaPlugin {
    
    private static VanillaSource plugin;

    private static ArtGUI artGUI;
    
    private static ImplVanillaSourceAPI api;

    private static boolean loadedSuccessfully = false;

    @Override
    public void onLoad() {
        try {
            super.onLoad();
            CommandRegistry.onLoad(this);
        } catch (Exception error) {
            error.printStackTrace();

            Logger.getLogger("VanillaSource").warning("Failed to load plugin : " + error.getMessage());
            Bukkit.getServer().shutdown();
        }
    }

    @Override
    public void onEnable() {
        try {
            // Plugin startup logic
            plugin = this;

            //Load config
            ImplVSSettings.load();

            //Load language files
            SystemLanguage.loadTexts();

            //NMS setup
            NMSManager.setup();

            //Setup gui
            artGUI = new ArtGUI(this);

            //Create api instance
            api = new ImplVanillaSourceAPI(this, NMSManager.getNMSHandler(), ImplVSSettings.getEntityThreads(), artGUI);

            //Load biomes
            BiomeStore.importVanillaBiomes();
            BiomeStore.loadCustomBiomes();

            //Start async tick runners
            TaskHandler.runSync(() -> {
                MainThreadTimer.instance.runTaskTimer(this, 0, 1);
                api.startAsyncThreads();
            });


            //Register event listeners
            getLogger().info("Registering event listeners...");
            PluginManager pluginManager = getServer().getPluginManager();
            pluginManager.registerEvents(new PlayerJoinQuitListener(), this);
            pluginManager.registerEvents(new ChunkListener(), this);
            pluginManager.registerEvents(new TestListener(), this);
            pluginManager.registerEvents(new CameraPositionSettingListener(), this);
            pluginManager.registerEvents(new PlayerClickListener(), this);
            pluginManager.registerEvents(new WorldListener(), this);


            //Register commands.
            if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
                //Register command executors
                getLogger().info("Registering command executors...");
                getCommand("parallel").setExecutor(new ParallelCommandExecutor());
                getCommand("parallel").setTabCompleter(new ParallelCommandExecutor());
            }
            getCommand("vanilla_source_hover_text_event").setExecutor(new HoverTextCommandExecutor());
            CommandRegistry.onEnable();

            //Load camera position data.
            CameraFileManager.load();

            ImplStructureData.loadAllStructureData();
            ParallelStructure.loadAllParallelStructure();

            WorldAssetsRegistry.init();

            //Load all Contan script
            try {
                ContanManager.loadAllModules();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Failed to load script files.");
            }

            //Create default universe
            api.createDefaultUniverse();

            //Start player tick timer
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                EnginePlayer.getAllPlayers().forEach(EngineEntity::tick);
            }, 0, 1);

            if (VSSettings.isOverrideModelEngineUpdater()) {
                ModelEngineAPI.getAPI().getModelUpdaters().end();
            }

            TextBoxManager.init(this);

            if (VSSettings.isDisableVanillaBGM()) {
                api.getNMSHandler().disableVanillaBGM();
            }

            loadedSuccessfully = true;
        } catch (Exception error) {
            error.printStackTrace();

            Logger.getLogger("VanillaSource").warning("Failed to load plugin : " + error.getMessage());
            Bukkit.getServer().shutdown();
        }
    }
    
    @Override
    public void onDisable() {
        if (!loadedSuccessfully) { return; }

        ContanManager.onDisable();

        // Plugin shutdown logic
        if(api != null) api.stopAsyncThreads();
        
        //CommandRegistry.unregister();
        
        BiomeStore.saveCustomBiomes();
        
        CameraFileManager.save();

        WorldAssetsRegistry.save();

        CommandRegistry.onDisable();
    }
    
    
    public static VanillaSource getPlugin(){
        return plugin;
    }

    public ArtGUI getArtGUI() {return artGUI;}
    
}

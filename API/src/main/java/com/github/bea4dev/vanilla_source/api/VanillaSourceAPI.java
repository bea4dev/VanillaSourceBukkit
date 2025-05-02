package com.github.bea4dev.vanilla_source.api;

import com.github.bea4dev.artgui.ArtGUI;
import com.github.bea4dev.vanilla_source.api.contan.ContanUtil;
import com.github.bea4dev.vanilla_source.api.contan.MainTickThread;
import com.github.bea4dev.vanilla_source.api.entity.tick.TickThreadPool;
import com.github.bea4dev.vanilla_source.api.entity.tick.TickWatchDog;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import org.bukkit.Bukkit;
import org.contan_lang.ContanEngine;
import com.github.bea4dev.vanilla_source.api.nms.INMSHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class VanillaSourceAPI {
    
    //API instance
    protected static VanillaSourceAPI instance;
    
    /**
     * Get api instance.
     * @return ParallelAPI
     */
    public static @NotNull VanillaSourceAPI getInstance() {return instance;}
    
    
    protected final JavaPlugin javaPlugin;
    
    protected final INMSHandler nmsHandler;
    
    protected final TickThreadPool tickThreadPool;
    
    protected final TickWatchDog watchDog;
    
    protected final ScheduledExecutorService watchFogExecutor;
    
    protected final ContanEngine contanEngine;
    
    protected final MainTickThread mainThread;
    
    protected final ArtGUI artGUI;

    protected ParallelUniverse defaultUniverse;
    
    public VanillaSourceAPI(JavaPlugin plugin, INMSHandler nmsHandler, int tickRunnerThreads, ArtGUI artGUI){
        this.javaPlugin = plugin;
        this.nmsHandler = nmsHandler;
        this.tickThreadPool = new TickThreadPool(tickRunnerThreads);
    
        this.watchDog = new TickWatchDog(tickThreadPool);
        this.watchFogExecutor = Executors.newSingleThreadScheduledExecutor();
        
        this.mainThread = new MainTickThread(0);
        Bukkit.getScheduler().runTaskTimer(plugin, mainThread, 0, 1);
        
        this.contanEngine = new ContanEngine(mainThread, new ArrayList<>(tickThreadPool.getAsyncTickRunnerList()));
        ContanUtil.setUpContan(contanEngine);
        
        this.artGUI = artGUI;
    }
    
    /**
     * Get plugin instance.
     * @return THPEngine plugin instance.
     */
    public JavaPlugin getPlugin() {return javaPlugin;}
    
    public INMSHandler getNMSHandler() {return nmsHandler;}
    
    public TickThreadPool getTickThreadPool() {return tickThreadPool;}
    
    public MainTickThread getMainThread() {return mainThread;}

    public ParallelUniverse getDefaultUniverse() {return defaultUniverse;}
    
    public ArtGUI getArtGUI() {return artGUI;}
    
    /**
     * Get Contan script engine.
     * @return Singleton {@link ContanEngine} instance.
     */
    public ContanEngine getContanEngine() {return contanEngine;}
    
    /**
     * Create universe if absent.
     * @param universeName Name of a universe
     * @return ParallelUniverse
     */
    public abstract @NotNull ParallelUniverse createUniverse(String universeName);
    
    /**
     * Get universe.
     * @param universeName Name of a universe
     * @return If the Universe with the specified name does not exist, return null.
     */
    public abstract @Nullable ParallelUniverse getUniverse(String universeName);
    
    /**
     * Remove universe with the specified name.
     * @param universeName Name of a universe.
     */
    public abstract void removeUniverse(String universeName);
    
    /**
     * Get all universe name.
     * @return All universe name
     */
    public abstract Set<String> getAllUniverseName();
    
    /**
     * Get all universe.
     * @return All universe
     */
    public abstract Collection<ParallelUniverse> getAllUniverse();
    
    /**
     * Get ParallelPlayer
     * @return ParallelPlayer
     */
    public @Nullable EnginePlayer getEnginePlayer(Player player){return EnginePlayer.getEnginePlayer(player);}
    
    public abstract boolean isHigher_v1_18_R1();
}

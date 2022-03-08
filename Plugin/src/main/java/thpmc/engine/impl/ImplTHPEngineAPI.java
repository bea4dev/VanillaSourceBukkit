package thpmc.engine.impl;

import be4rjp.parallel.impl.ImplParallelUniverse;
import be4rjp.parallel.nms.NMSManager;
import org.bukkit.plugin.java.JavaPlugin;
import thpmc.engine.api.THPEngineAPI;
import be4rjp.parallel.ParallelUniverse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.nms.INMSHandler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ImplTHPEngineAPI extends THPEngineAPI {
    
    //All universes
    private static final Map<String, ParallelUniverse> universeMap = new ConcurrentHashMap<>();
    
    public ImplTHPEngineAPI(JavaPlugin plugin, INMSHandler nmsHandler, int tickRunnerThreads) {
        super(plugin, nmsHandler, tickRunnerThreads);
        instance = this;
    }
    
    @Override
    public @NotNull ParallelUniverse createUniverse(String universeName) {
        return universeMap.computeIfAbsent(universeName, ImplParallelUniverse::new);
    }
    
    @Override
    public @Nullable ParallelUniverse getUniverse(String universeName) {return universeMap.get(universeName);}
    
    @Override
    public void removeUniverse(String universeName) {
        ParallelUniverse universe = getUniverse(universeName);
        if(universe != null) ((ImplParallelUniverse) universe).getPlayers().forEach(player -> player.setUniverse(null));
    
        universeMap.remove(universeName);
    }
    
    @Override
    public Set<String> getAllUniverseName() {return universeMap.keySet();}
    
    @Override
    public Collection<ParallelUniverse> getAllUniverse() {return universeMap.values();}
    
    @Override
    public boolean isHigher_v1_18_R1() {return NMSManager.isHigher_v1_18_R1();}
    
    private boolean isStarted = false;
    
    public synchronized void startAsyncThreads(){
        if(isStarted) return;
        isStarted = true;
        
        super.tickRunnerPool.startAll();
        super.watchFogExecutor.scheduleAtFixedRate(watchDog, 0, 30, TimeUnit.SECONDS);
    }
    
    public synchronized void stopAsyncThreads(){
        if(!isStarted) return;
        isStarted = false;
        
        super.tickRunnerPool.cancelAll();
        super.watchDog.cancel();
        super.watchFogExecutor.shutdown();
    }
    
}

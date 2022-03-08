package thpmc.engine.api.entity.tick;

import thpmc.engine.api.entity.EngineEntity;
import thpmc.engine.api.entity.TickBase;
import thpmc.engine.api.player.EnginePlayer;
import thpmc.engine.api.world.cache.local.ThreadLocalCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TickRunner implements Runnable {
    
    private static final Set<TickRunner> tickRunners = ConcurrentHashMap.newKeySet();
    
    public static void removeTrackers(EnginePlayer enginePlayer){
        for(TickRunner tickRunner : tickRunners){
            tickRunner.removeTracker(enginePlayer);
        }
    }
    
    
    public static final int TPS = 20;
    public static final long TIME = 1000 / TPS;
    
    private final Set<EngineEntity> entities = new HashSet<>();
    
    private final Set<TickBase> tickOnlyEntities = new HashSet<>();
    
    private final Set<TickBase> addEntities = new HashSet<>();
    
    private final ReentrantLock ADD_LOCK = new ReentrantLock();
    
    private final ThreadLocalCache threadLocalCache = new ThreadLocalCache();
    
    private boolean isStopped = false;
    
    private final int ID;
    
    public TickRunner(int ID){this.ID = ID;}
    
    public int getRunnerID() {return ID;}
    
    /**
     * Get cache of thread-local worlds.
     * @return {@link ThreadLocalCache}
     */
    public ThreadLocalCache getThreadLocalCache() {return threadLocalCache;}
    
    
    public void addEntity(TickBase tickBaseEntity){
        try {
            ADD_LOCK.lock();
            this.addEntities.add(tickBaseEntity);
        }finally {
            ADD_LOCK.unlock();
        }
    }
    
    private int i = 0;
    
    private long lastTickMS = System.currentTimeMillis();
    
    private long beforeTime = System.currentTimeMillis();
    
    private double tps = TPS;
    
    public long getLastTickMS() {return lastTickMS;}
    
    public double getTPS() {return tps;}
    
    private Thread currentThread = Thread.currentThread();
    
    /**
     * Gets the current thread executing tick.
     * @return {@link Thread}
     */
    public Thread getCurrentThread() {return currentThread;}
    
    
    private final Map<EnginePlayer, EntityTracker> trackerMap = new HashMap<>();
    
    public EntityTracker getEntityTracker(EnginePlayer enginePlayer){
        return trackerMap.computeIfAbsent(enginePlayer, ep -> new EntityTracker(this, ep));
    }
    
    private void removeTracker(EnginePlayer enginePlayer){trackerMap.remove(enginePlayer);}
    
    @Override
    public void run() {
        if(beforeTime + TIME - 20 > System.currentTimeMillis()) return;
        
        if(isStopped) return;
        
        if(i % TPS == 0){
            long time = System.currentTimeMillis();
            tps = ((double)Math.round((20.0 / (((double) (time - beforeTime)) / 1000.0)) * 10))/10;
            beforeTime = time;
        }
        
        currentThread = Thread.currentThread();
        
        this.lastTickMS = System.currentTimeMillis();
        
        //Should remove check
        entities.removeIf(TickBase::shouldRemove);
        tickOnlyEntities.removeIf(TickBase::shouldRemove);
    
        //Add entities
        if(addEntities.size() != 0) {
            try {
                ADD_LOCK.lock();
        
                for (TickBase entity : addEntities) {
                    if (entity instanceof EngineEntity) {
                        entities.add((EngineEntity) entity);
                    } else {
                        tickOnlyEntities.add(entity);
                    }
                }
        
                addEntities.clear();
            } finally {
                ADD_LOCK.unlock();
            }
        }
        
        //tick
        tickOnlyEntities.forEach(TickBase::tick);
        entities.forEach(TickBase::tick);
    
        //Tracker
        for(EnginePlayer enginePlayer : EnginePlayer.getAllPlayers()){
            EntityTracker entityTracker = getEntityTracker(enginePlayer);
            entityTracker.tick(entities);
        }
        if(i % 40 == 0){
            trackerMap.keySet().removeIf(enginePlayer -> !enginePlayer.getBukkitPlayer().isOnline());
        }
        
        entities.forEach(EngineEntity::setPreviousPosition);
        
        if(i % 100 == 0) System.out.printf("ID -> %d   TPS: %.1f%n", ID, tps);
        i++;
    }
    
    public void cancel() {isStopped = true;}
}

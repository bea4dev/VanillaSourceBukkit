package com.github.bea4dev.vanilla_source.api.entity.tick;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.contan_lang.ContanEngine;
import org.contan_lang.thread.ContanTickBasedThread;
import org.contan_lang.variables.primitive.ContanFunctionExpression;
import org.jetbrains.annotations.NotNull;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.entity.EngineEntity;
import com.github.bea4dev.vanilla_source.api.entity.TickBase;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.world.cache.local.ThreadLocalCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class TickThread implements Runnable, ContanTickBasedThread {
    public static final int TPS = 20;
    public static final long TIME = 1000 / TPS;

    //tick executor
    private final ExecutorService tickExecutor = Executors.newSingleThreadExecutor();

    private final Set<EngineEntity> entities = new HashSet<>();
    
    private final Set<TickBase> tickOnlyEntities = new HashSet<>();
    
    private final Set<TickBase> addEntities = new HashSet<>();

    private final Set<Chunk> releaseChunks = new HashSet<>();
    
    private final ReentrantLock ADD_LOCK = new ReentrantLock();
    
    private final ThreadLocalCache threadLocalCache = new ThreadLocalCache(this);
    
    private boolean isStopped = false;
    
    private final int ID;
    
    public TickThread(int ID) { this.ID = ID; }
    
    public int getRunnerID() { return ID; }
    
    /**
     * Get cache of thread-local worlds.
     * @return {@link ThreadLocalCache}
     */
    public ThreadLocalCache getThreadLocalCache() {return threadLocalCache;}
    
    
    public void addEntity(TickBase tickBaseEntity){
        try {
            ADD_LOCK.lock();
            this.addEntities.add(tickBaseEntity);
        } finally {
            ADD_LOCK.unlock();
        }
    }

    public void releaseChunk(Chunk chunk) {
        try {
            ADD_LOCK.lock();
            this.releaseChunks.add(chunk);
        } finally {
            ADD_LOCK.unlock();
        }
    }
    
    private int i = 0;
    
    private long lastTickMS = System.currentTimeMillis();
    
    private long beforeTime = System.currentTimeMillis();
    
    private double tps = TPS;
    
    public long getLastTickMS() { return lastTickMS; }
    
    public double getTPS() { return tps; }
    
    private Thread currentThread = Thread.currentThread();
    
    public void removeEngineEntityUnsafe(EngineEntity entity) { entities.remove(entity); }
    
    /**
     * Gets the current thread executing tick.
     * @return {@link Thread}
     */
    public Thread getCurrentThread() { return currentThread; }
    
    
    private final Map<EnginePlayer, EntityTracker> trackerMap = new HashMap<>();
    
    public EntityTracker getEntityTracker(EnginePlayer enginePlayer){
        return trackerMap.computeIfAbsent(enginePlayer, ep -> new EntityTracker(this, ep));
    }
    
    public void removeTracker(EnginePlayer enginePlayer) { trackerMap.remove(enginePlayer); }
    
    @Override
    public void run() {
        if (beforeTime + TIME - 20 > System.currentTimeMillis()) return;
        
        if (isStopped) return;
        
        if (i % TPS == 0) {
            long time = System.currentTimeMillis();
            tps = ((double)Math.round((20.0 / (((double) (time - beforeTime)) / 1000.0)) * 10))/10;
            beforeTime = time;
        }
        
        currentThread = Thread.currentThread();
        
        this.lastTickMS = System.currentTimeMillis();
        
        //Should remove check
        entities.removeIf(entity -> {
            boolean shouldRemove = entity.shouldRemove();
            if (shouldRemove) {
                for (EnginePlayer enginePlayer : EnginePlayer.getAllPlayers()) {
                    EntityTracker entityTracker = getEntityTracker(enginePlayer);
                    entityTracker.removeTrackedEntity(entity);
                    entity.hide(enginePlayer);
                }
            }
            return shouldRemove;
        });
        tickOnlyEntities.removeIf(TickBase::shouldRemove);
    
    
        boolean forceTrack = false;
        
        //Add entities
        if (!addEntities.isEmpty()) {
            try {
                ADD_LOCK.lock();
        
                for (TickBase entity : addEntities) {
                    if (entity instanceof EngineEntity) {
                        entities.add((EngineEntity) entity);
                        forceTrack = true;
                    } else {
                        tickOnlyEntities.add(entity);
                    }
                }
        
                addEntities.clear();
            } finally {
                ADD_LOCK.unlock();
            }
        }

        // Release chunk
        if (!releaseChunks.isEmpty()) {
            try {
                ADD_LOCK.lock();

                for (Chunk chunk : releaseChunks) {
                    threadLocalCache.releaseChunk(chunk);
                }
                releaseChunks.clear();
            } finally {
                ADD_LOCK.unlock();
            }
        }
        
        //tick
        for (var tickBase : tickOnlyEntities) {
            try {
                tickBase.tick();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        for (var tickBase : entities) {
            try {
                tickBase.tick();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    
        //Tracker
        for(EnginePlayer enginePlayer : EnginePlayer.getAllPlayers()){
            EntityTracker entityTracker = getEntityTracker(enginePlayer);
            entityTracker.tick(entities, forceTrack);
        }
        if(i % 40 == 0){
            //Remove offline player
            trackerMap.keySet().removeIf(enginePlayer -> !enginePlayer.getBukkitPlayer().isOnline());
        }
        
        entities.forEach(EngineEntity::setPreviousPosition);
        i++;
    }
    
    public void cancel() {
        isStopped = true;
        MainThreadTimer.instance.removeTickRunner(this);
        tickExecutor.shutdown();
    }

    public void start(){
        this.lastTickMS = System.currentTimeMillis();
        MainThreadTimer.instance.addTickRunner(this);
    }

    public void tickAtAsync(){
        tickExecutor.submit(() -> {
            try {
                this.run();
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }
    
    public void runTaskLater(ContanFunctionExpression functionExpression, long delay) {
        Runnable task = () -> {
            functionExpression.eval(this, functionExpression.getBasedJavaObject().getFunctionName());
        };
        Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), () -> tickExecutor.submit(task), delay);
    }
    
    public void runTaskTimer(ContanFunctionExpression functionExpression, long delay, long period) {
        Runnable task = () -> {
            functionExpression.eval(this, functionExpression.getBasedJavaObject().getFunctionName());
        };
        Bukkit.getScheduler().runTaskTimer(VanillaSourceAPI.getInstance().getPlugin(), () -> tickExecutor.submit(task), delay, period);
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable, long l) {
        ThreadFutureTask<T> task = new ThreadFutureTask<>(callable);
        Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), () -> tickExecutor.submit(task), l);
    }
    
    @Override
    public <T> T runTaskImmediately(Callable<T> callable) throws ExecutionException, InterruptedException {
        return tickExecutor.submit(callable).get();
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable) {
        ThreadFutureTask<T> task = new ThreadFutureTask<>(callable);
        tickExecutor.submit(task);
    }
    
    public void scheduleTask(Runnable task) {
        if (task instanceof TickThreadRunnable) {
            ((TickThreadRunnable) task).runTask();
        } else {
            new TickThreadRunnable(this) {
                @Override
                public void run() {
                    task.run();
                }
            }.runTask();
        }
    }
    
    public void scheduleTask(Runnable task, long delay) {
        if (task instanceof TickThreadRunnable) {
            ((TickThreadRunnable) task).runTask();
        } else {
            new TickThreadRunnable(this) {
                @Override
                public void run() {
                    task.run();
                }
            }.runTaskLater(delay);
        }
    }
    
    public void scheduleTask(Runnable task, long delay, long period) {
        if (task instanceof TickThreadRunnable) {
            ((TickThreadRunnable) task).runTask();
        } else {
            new TickThreadRunnable(this) {
                @Override
                public void run() {
                    task.run();
                }
            }.runTaskTimer(delay, period);
        }
    }
    
    @Override
    public boolean shutdownWithAwait(long l, TimeUnit timeUnit) throws InterruptedException {
        return true;
    }
    
    @Override
    public ContanEngine getContanEngine() {
        return VanillaSourceAPI.getInstance().getContanEngine();
    }


    public static class ThreadFutureTask<T> extends FutureTask<T> {

        public ThreadFutureTask(@NotNull Callable<T> callable) {
            super(callable);
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) get();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

}


package com.github.bea4dev.vanilla_source.impl;

import org.bukkit.Location;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.evaluators.ClassBlock;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import org.jetbrains.annotations.NotNull;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.entity.tick.TickThread;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ImplEnginePlayer extends EnginePlayer {

    public static EnginePlayer onPlayerJoin(Player player){
        return playerMap.computeIfAbsent(player, ImplEnginePlayer::new);
    }

    public static void onPlayerQuit(Player player){
        EnginePlayer enginePlayer = playerMap.get(player);
        enginePlayer.setUniverse(VanillaSourceAPI.getInstance().getDefaultUniverse());
        playerMap.remove(player);

        for (var thread : VanillaSourceAPI.getInstance().getTickThreadPool().getAsyncTickRunnerList()) {
            thread.removeTracker(enginePlayer);
        }
    }


    private ImplEnginePlayer(Player player) {
        super(player, VanillaSourceAPI.getInstance().getMainThread().getThreadLocalCache().getParallelWorld(
                VanillaSourceAPI.getInstance().getDefaultUniverse(), player.getWorld().getName()),
                new PlayerEntityController(player), VanillaSourceAPI.getInstance().getMainThread(), null);
        
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
    
        ContanClassInstance scriptHandle = null;
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/entity/Player.cntn");
        if (contanModule != null) {
            ClassBlock classBlock = contanModule.getClassByName("Player");
            if (classBlock != null) {
                scriptHandle = classBlock.createInstance(contanEngine, tickThread, new JavaClassInstance(contanEngine, this));
            }
        }
        
        super.scriptHandle = scriptHandle;
        super.invokeScriptFunction("setup");
        
        super.currentUniverse = VanillaSourceAPI.getInstance().getDefaultUniverse();
    }

    @Override
    public synchronized @NotNull ParallelUniverse getUniverse() {return currentUniverse;}

    @Override
    public synchronized void setUniverse(@NotNull ParallelUniverse parallelUniverse) {
        if(currentUniverse == parallelUniverse) return;

        if(currentUniverse != null){
            ((ImplParallelUniverse) currentUniverse).getPlayers().remove(this);

            ParallelWorld currentWorld = currentUniverse.getWorld(player.getWorld().getName());
            this.currentUniverse = parallelUniverse;

            int range = Bukkit.getViewDistance();

            int chunkX = player.getLocation().getBlockX() >> 4;
            int chunkZ = player.getLocation().getBlockZ() >> 4;

            for(int x = -range; x < range; x++){
                for(int z = -range; z < range; z++){
                    ParallelChunk chunk = currentWorld.getChunk(chunkX + x, chunkZ + z);
                    if(chunk == null) continue;

                    ((ImplParallelChunk) chunk).sendClearPacket(player);
                }
            }
        }


        ((ImplParallelUniverse) parallelUniverse).getPlayers().add(this);

        ParallelWorld nextWorld = parallelUniverse.getWorld(player.getWorld().getName());
        this.currentUniverse = parallelUniverse;

        int range = Bukkit.getViewDistance();

        int chunkX = player.getLocation().getBlockX() >> 4;
        int chunkZ = player.getLocation().getBlockZ() >> 4;

        for(int x = -range; x < range; x++){
            for(int z = -range; z < range; z++){
                ParallelChunk chunk = nextWorld.getChunk(chunkX + x, chunkZ + z);
                if(chunk == null) continue;

                chunk.sendUpdate(player);
            }
        }

        this.currentUniverse = parallelUniverse;
    }
    
    public void setUniverseRaw(@NotNull ParallelUniverse universe){this.currentUniverse = universe;}
    
    @Override
    public void teleport(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        player.teleport(new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch));
        super.teleport(worldName, x, y, z, yaw, pitch);
    }

    /**
     * Main thread tick task.
     */
    @Override
    public void tick() {
        invokeScriptFunction("update1");
        invokeScriptFunction("onTick");

        super.currentLocation = player.getLocation();
        super.setPosition(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
        super.setRotation(currentLocation.getYaw(), currentLocation.getPitch());

        invokeScriptFunction("update2");
    }
    
    @Override
    public void spawn() {
        //invokeScriptFunction("update");
    }
    
    @Override
    public void switchUniverse(ParallelUniverse universe) {
        if (universe == null) {
            throw new IllegalArgumentException("null cannot be specified.");
        }
        
        super.switchUniverse(universe);
        this.setUniverse(universe);
    }
}

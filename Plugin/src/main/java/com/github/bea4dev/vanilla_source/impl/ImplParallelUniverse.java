package com.github.bea4dev.vanilla_source.impl;

import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.evaluators.ClassBlock;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassInstance;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelChunk;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelWorld;
import com.github.bea4dev.vanilla_source.nms.NMSManager;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.util.SectionLevelArray;
import com.github.bea4dev.vanilla_source.api.util.SectionTypeArray;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelUniverse implements ParallelUniverse {

    private final String universeName;

    private final Set<EnginePlayer> players = ConcurrentHashMap.newKeySet();

    private final ContanClassInstance scriptHandle;

    public ImplParallelUniverse(String universeName){
        this.universeName = universeName;

        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();
        ContanClassInstance scriptHandle = null;
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/world/Universe.cntn");
        if (contanModule != null) {
            ClassBlock classBlock = contanModule.getClassByName("Universe");
            if (classBlock != null) {
                scriptHandle = classBlock.createInstance(contanEngine, contanEngine.getMainThread(), new JavaClassInstance(contanEngine, this));
            }
        }
        this.scriptHandle = scriptHandle;
    }

    @Override
    public @NotNull String getName() {return universeName;}


    private final Map<String, ParallelWorld> parallelWorldMap = new ConcurrentHashMap<>();

    @Override
    public @NotNull ParallelWorld getWorld(String worldName) {
        return parallelWorldMap.computeIfAbsent(worldName, name -> new ImplParallelWorld(this, worldName));
    }

    @Override
    public void addPlayer(@NotNull EnginePlayer player) {player.setUniverse(this);}

    @Override
    public void removePlayer(@NotNull EnginePlayer player) {player.setUniverse(VanillaSourceAPI.getInstance().getDefaultUniverse());}

    @Override
    public Set<EnginePlayer> getResidents() {return new HashSet<>(players);}
    
    @Override
    public Collection<ParallelWorld> getAllWorld() {return parallelWorldMap.values();}
    
    @Override
    public void addDiffs(ParallelUniverse universe) {
        int indexStart = NMSManager.isHigher_v1_18_R1() ? -4 : 0;
        int indexEnd = NMSManager.isHigher_v1_18_R1() ? 20 : 16;
        
        for(ParallelWorld diffWorld : universe.getAllWorld()){
            for(ParallelChunk diffChunk : diffWorld.getAllChunk()){
                for(int i = indexStart; i < indexEnd; i++){
                    ParallelWorld thisWorld = null;
                    ParallelChunk thisChunk = null;
            
                    SectionTypeArray sectionTypeArray = diffChunk.getSectionTypeArray(i);
                    if(sectionTypeArray != null) {
                        thisWorld = this.getWorld(diffWorld.getName());
                        thisChunk = ((ImplParallelWorld) thisWorld).createChunkIfAbsent(diffChunk.getChunkX(), diffChunk.getChunkZ());
                        SectionTypeArray thisType = ((ImplParallelChunk) thisChunk).createSectionTypeArrayIfAbsent(i);
                        
                        sectionTypeArray.threadsafeIteration(thisType::setType);
                    }
    
                    SectionLevelArray blockLightLevelArray = diffChunk.getBlockLightSectionLevelArray(i);
                    if(blockLightLevelArray != null){
                        if(thisWorld == null) thisWorld = this.getWorld(diffWorld.getName());
                        if(thisChunk == null) thisChunk = ((ImplParallelWorld) thisWorld).createChunkIfAbsent(diffChunk.getChunkX(), diffChunk.getChunkZ());
                        SectionLevelArray thisLevel = ((ImplParallelChunk) thisChunk).createBlockLightSectionLevelArrayIfAbsent(i);
    
                        blockLightLevelArray.threadsafeIteration(thisLevel::setLevel);
                    }
    
                    SectionLevelArray skyLightLevelArray = diffChunk.getSkyLightSectionLevelArray(i);
                    if(skyLightLevelArray != null){
                        if(thisWorld == null) thisWorld = this.getWorld(diffWorld.getName());
                        if(thisChunk == null) thisChunk = ((ImplParallelWorld) thisWorld).createChunkIfAbsent(diffChunk.getChunkX(), diffChunk.getChunkZ());
                        SectionLevelArray thisLevel = ((ImplParallelChunk) thisChunk).createSkyLightSectionLevelArrayIfAbsent(i);
        
                        skyLightLevelArray.threadsafeIteration(thisLevel::setLevel);
                    }
                }
            }
        }
        
        
        for(EnginePlayer EnginePlayer : this.getResidents()){
            ((ImplEnginePlayer) EnginePlayer).setUniverseRaw(VanillaSourceAPI.getInstance().getDefaultUniverse());
            EnginePlayer.setUniverse(this);
        }
    }

    @Override
    public ContanClassInstance getScriptHandle() {
        return scriptHandle;
    }

    public Set<EnginePlayer> getPlayers() {return players;}
}

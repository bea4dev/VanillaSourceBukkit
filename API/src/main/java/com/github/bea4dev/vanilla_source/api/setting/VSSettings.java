package com.github.bea4dev.vanilla_source.api.setting;

public abstract class VSSettings {
    
    protected static boolean useCachedChunkPacket = false;
    
    protected static boolean rewriteLightPacket = true;
    
    protected static int entityThreads = 1;
    
    protected static boolean useChiyogamiParallelBridge = true;
    
    public static boolean isUseCachedChunkPacket() {return useCachedChunkPacket;}
    
    public static boolean isRewriteLightPacket() {return rewriteLightPacket;}
    
    public static int getEntityThreads() {return entityThreads;}
    
    public static boolean isUseChiyogamiParallelBridge() {return useChiyogamiParallelBridge;}
    
}

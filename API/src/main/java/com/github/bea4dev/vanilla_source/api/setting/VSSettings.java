package com.github.bea4dev.vanilla_source.api.setting;

public abstract class VSSettings {

    protected static boolean useCachedChunkPacket = false;

    protected static boolean rewriteLightPacket = true;

    protected static int entityThreads = 1;

    protected static boolean overrideModelEngineUpdater = false;

    protected static boolean useChiyogamiParallelBridge = true;

    protected static boolean disableVanillaBGM = false;

    public static boolean isUseCachedChunkPacket() {
        return useCachedChunkPacket;
    }

    public static boolean isRewriteLightPacket() {
        return rewriteLightPacket;
    }

    public static int getEntityThreads() {
        return entityThreads;
    }

    public static boolean isUseChiyogamiParallelBridge() {
        return useChiyogamiParallelBridge;
    }

    public static boolean isOverrideModelEngineUpdater() {
        return overrideModelEngineUpdater;
    }

    public static boolean isDisableVanillaBGM() {
        return disableVanillaBGM;
    }
}

package com.github.bea4dev.vanilla_source.nms;

import org.bukkit.Bukkit;
import com.github.bea4dev.vanilla_source.api.nms.INMSHandler;
import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;

import java.lang.reflect.InvocationTargetException;

public class NMSManager {

    private static String version;

    private static Class<?> getImplClass(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return Class.forName("com.github.bea4dev.vanilla_source.nms." + version + "." + className);
    }

    public static String getVersion() {
        return version;
    }


    private static INMSHandler nmsHandler;

    public static INMSHandler getNMSHandler() {
        return nmsHandler;
    }


    private static IPacketHandler mapChunkPacketHandler;
    private static IPacketHandler blockChangePacketHandler;
    private static IPacketHandler multiBlockChangePacketHandler;
    private static IPacketHandler lightUpdatePacketHandler;
    private static IPacketHandler flyPacketHandler;
    private static IPacketHandler playerInputPacketHandler;
    private static IPacketHandler windowContentsPacketHandler;

    public static IPacketHandler getBlockChangePacketHandler() {
        return blockChangePacketHandler;
    }

    public static IPacketHandler getMapChunkPacketHandler() {
        return mapChunkPacketHandler;
    }

    public static IPacketHandler getFlyPacketHandler() {
        return flyPacketHandler;
    }

    public static IPacketHandler getMultiBlockChangePacketHandler() {
        return multiBlockChangePacketHandler;
    }

    public static IPacketHandler getLightUpdatePacketHandler() {
        return lightUpdatePacketHandler;
    }

    public static IPacketHandler getPlayerInputPacketHandler() {
        return playerInputPacketHandler;
    }

    public static IPacketHandler getWindowContentsPacketHandler() {
        return windowContentsPacketHandler;
    }


    public static void setup() {
        String versionName = Bukkit.getServer().getMinecraftVersion();
        if (versionName.equals("1.21.1")) {
            version = "v1_21_R1";
        } else if (versionName.equals("1.21.5")) {
            version = "v1_21_R4";
        } else {
            throw new IllegalStateException("This version is not supported!" + System.lineSeparator() + "Server version : " + versionName);
        }

        try {
            Class<?> nmsHandlerClass = getImplClass("NMSHandler");
            nmsHandler = (INMSHandler) nmsHandlerClass.getConstructor().newInstance();

            Class<?> MapChunkPacketHandler = getImplClass("MapChunkPacketHandler");
            Class<?> BlockChangePacketHandler = getImplClass("BlockChangePacketHandler");
            Class<?> MultiBlockChangePacketHandler = getImplClass("MultiBlockChangePacketHandler");
            Class<?> LightUpdatePacketHandler = getImplClass("LightUpdatePacketHandler");
            Class<?> FlyPacketHandler = getImplClass("FlyPacketHandler");
            Class<?> PlayerInputPacketHandler = getImplClass("PlayerInputPacketHandler");
            Class<?> WindowContentsPacketHandler = getImplClass("WindowContentsPacketHandler");

            mapChunkPacketHandler = (IPacketHandler) MapChunkPacketHandler.getConstructor().newInstance();
            blockChangePacketHandler = (IPacketHandler) BlockChangePacketHandler.getConstructor().newInstance();
            multiBlockChangePacketHandler = (IPacketHandler) MultiBlockChangePacketHandler.getConstructor().newInstance();
            lightUpdatePacketHandler = (IPacketHandler) LightUpdatePacketHandler.getConstructor().newInstance();
            flyPacketHandler = (IPacketHandler) FlyPacketHandler.getConstructor().newInstance();
            playerInputPacketHandler = (IPacketHandler) PlayerInputPacketHandler.getConstructor().newInstance();
            windowContentsPacketHandler = (IPacketHandler) WindowContentsPacketHandler.getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            e.printStackTrace();

            throw new IllegalStateException("This version is not supported!" + System.lineSeparator() + "Server version : " + versionName);
        }
    }

}

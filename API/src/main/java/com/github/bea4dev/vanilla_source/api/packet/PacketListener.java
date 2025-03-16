package com.github.bea4dev.vanilla_source.api.packet;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class PacketListener {
    private static List<BiFunction<Player, Object, Object>> readListeners = new ArrayList<>();
    private static List<BiFunction<Player, Object, Object>> writeListeners = new ArrayList<>();

    @SafeVarargs
    public static void registerReadListener(BiFunction<Player, Object, Object>... listeners) {
        readListeners.addAll(Arrays.asList(listeners));
    }

    @SafeVarargs
    public static void registerWriteListener(BiFunction<Player, Object, Object>... listeners) {
        writeListeners.addAll(Arrays.asList(listeners));
    }

    public static Object onPacketReceived(Player player, Object packet) {
        var newPacket = packet;
        for (BiFunction<Player, Object, Object> listener : readListeners) {
            newPacket = listener.apply(player, newPacket);
        }
        return newPacket;
    }

    public static Object onPacketSend(Player player, Object packet) {
        var newPacket = packet;
        for (BiFunction<Player, Object, Object> listener : writeListeners) {
            newPacket = listener.apply(player, newPacket);
        }
        return newPacket;
    }
}

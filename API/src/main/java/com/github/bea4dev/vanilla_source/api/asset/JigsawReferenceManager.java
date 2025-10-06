package com.github.bea4dev.vanilla_source.api.asset;

import org.bukkit.NamespacedKey;

import java.util.*;

public class JigsawReferenceManager {
    private static final Map<NamespacedKey, List<JigsawReference>> nameReferenceMap = new HashMap<>();
    private static final Map<String, List<JigsawReference>> assetReferenceMap = new HashMap<>();

    static void registerReference(JigsawReference reference) {
        nameReferenceMap.computeIfAbsent(reference.jigsawState().name(), (n) -> new ArrayList<>()).add(reference);
        assetReferenceMap.computeIfAbsent(reference.worldAsset().getAssetName(), (n) -> new ArrayList<>()).add(reference);
    }

    public static List<JigsawReference> getFromName(NamespacedKey key) {
        var list = nameReferenceMap.get(key);
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public static Collection<NamespacedKey> getJigsawNames() {
        return nameReferenceMap.keySet();
    }

    public static List<JigsawReference> getFromAsset(String assetName) {
        var list = assetReferenceMap.get(assetName);
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public static void clear() {
        nameReferenceMap.clear();
        assetReferenceMap.clear();
    }
}

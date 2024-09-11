package com.github.bea4dev.vanilla_source.biome;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.nms.INMSHandler;
import com.github.bea4dev.vanilla_source.util.RegionBlocks;
import com.github.bea4dev.vanilla_source.world_edit.WorldEditUtil;
import org.bukkit.entity.Player;
import com.github.bea4dev.vanilla_source.biome.gui.BiomeGUI;
import com.github.bea4dev.vanilla_source.lang.SystemLanguage;

public class BiomeManager {

    public static void setBiome(Player player) {
        RegionBlocks regionBlocks = WorldEditUtil.getSelectedRegion(player);
        if (regionBlocks == null) {
            return;
        }

        BiomeGUI.openBiomeSelectGUI(player, SystemLanguage.getText("select-biome"), biomeSource -> {
            Object nmsBiome = biomeSource.getNMSBiome();
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
            regionBlocks.apply(block -> nmsHandler.setBiomeForBlock(block, nmsBiome));

            player.sendMessage(SystemLanguage.getText("biome-applied"));
            player.closeInventory();
        });
    }

}

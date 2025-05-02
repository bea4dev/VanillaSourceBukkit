package com.github.bea4dev.vanilla_source.biome.gui;

import com.github.bea4dev.artgui.button.*;
import com.github.bea4dev.artgui.frame.Artist;
import com.github.bea4dev.artgui.menu.ArtMenu;
import com.github.bea4dev.artgui.menu.HistoryData;
import com.github.bea4dev.vanilla_source.VanillaSource;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.biome.BiomeDataContainer;
import com.github.bea4dev.vanilla_source.api.biome.BiomeSource;
import com.github.bea4dev.vanilla_source.api.biome.BiomeStore;
import com.github.bea4dev.vanilla_source.api.biome.CustomBiome;
import com.github.bea4dev.vanilla_source.api.nms.INMSHandler;
import com.github.bea4dev.vanilla_source.api.util.gui.TextInputButton;
import com.github.bea4dev.vanilla_source.lang.SystemLanguage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class BiomeGUI {

    public static void openBiomeSelectGUI(Player player, String title, Consumer<BiomeSource> onSelect) {
        Artist artist = new Artist(() -> {
            //ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
            
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
            
            //PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
            
            //PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
            
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
            
            //ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
            
            ArtButton M = new ArtButton(new ItemBuilder(Material.GRASS_BLOCK).name(SystemLanguage.getText("gui-biome-minecraft")).build());
            M.listener((event, menu) -> openMinecraftBiomeSelectGUI(player, title, onSelect));
            
            ArtButton C = new ArtButton(new ItemBuilder(Material.COMMAND_BLOCK).name(SystemLanguage.getText("gui-biome-custom")).build());
            C.listener((event, menu) -> openCustomBiomeSelectGUI(player, title, onSelect));
            
            return new ArtButton[]{
                    G, G, G, G, G, G, G, G, G,
                    G, G, M, G, G, G, C, G, B,
                    G, G, G, G, G, G, G, G, E,
            };
        });
        
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
        
        artMenu.asyncCreate(menu -> {});
        
        artMenu.open(player);
    }
    
    
    public static void openMinecraftBiomeSelectGUI(Player player, String title, Consumer<BiomeSource> onSelect) {
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
        
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
        
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
        
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
        
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
        
            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, I,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, B,
                    V, V, V, V, V, V, V, G, E,
            };
        });
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
    
        artMenu.asyncCreate(menu -> {
            for (BiomeSource biomeSource : BiomeStore.getAllVanillaBiomes()) {
                ArtButton biomeButton = new ArtButton(new ItemBuilder(Material.GRASS_BLOCK).name(biomeSource.getKey()).build());
                biomeButton.listener((e, m) -> {
                    onSelect.accept(biomeSource);
                });
                menu.addButton(biomeButton);
            }
        });
    
        artMenu.open(player);
    }
    
    
    public static void openCustomBiomeSelectGUI(Player player, String title, Consumer<BiomeSource> onSelect) {
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
        
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
        
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
        
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
        
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
            
            TextInputButton C = new TextInputButton(new ItemBuilder(Material.COMMAND_BLOCK)
                    .name(SystemLanguage.getText("gui-create-custom-biome")).build(),
                    SystemLanguage.getText("gui-input-custom-biome-name"),
                    SystemLanguage.getText("gui-input-custom-biome-name-item"));
            C.onInput((p, text) -> {
                if (!text.matches("^[0-9a-zA-Z]+$")) {
                    player.sendMessage(SystemLanguage.getText("gui-input-custom-biome-name-incorrect"));
                    return "Incorrect name.";
                }
                
                String key = "custom:" + text;
                INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
                BiomeDataContainer container = new BiomeDataContainer();
                nmsHandler.setDefaultBiomeData(container);
                
                Object nmsBiome = nmsHandler.createBiome(text, container);
                CustomBiome customBiome = new CustomBiome(key, nmsBiome, container, new File("plugins/VanillaSource/biomes/" + text + ".yml"));
                BiomeStore.registerCustomBiome(customBiome);
                
                Bukkit.getScheduler().runTaskLater(VanillaSource.getPlugin(), () -> {
                    HistoryData historyData = HistoryData.getHistoryData(VanillaSource.getPlugin().getArtGUI(), player);
                    historyData.clearOnClose = true;
                    
                    openCustomBiomeEditor(player, customBiome);
                }, 1);
                return "close";
            });
            
        
            return new ArtButton[]{
                    V, V, V, V, V, V, G, C, N,
                    V, V, V, V, V, V, G, G, I,
                    V, V, V, V, V, V, G, G, P,
                    V, V, V, V, V, V, G, G, B,
                    V, V, V, V, V, V, G, G, E,
            };
        });
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
    
        artMenu.asyncCreate(menu -> {
            for (CustomBiome customBiome : BiomeStore.getAllCustomBiomes()) {
                ArtButton biomeButton = new ArtButton(new ItemBuilder(Material.GRASS_BLOCK)
                        .name(customBiome.getKey()).lore(SystemLanguage.getText("gui-right-click-to-edit")).build());
                biomeButton.listener((e, m) -> {
                    if (e.getClick() == ClickType.LEFT) {
                        onSelect.accept(customBiome);
                    } else if (e.getClick() == ClickType.RIGHT) {
                        openCustomBiomeEditor(player, customBiome);
                    }
                });
                menu.addButton(biomeButton);
            }
        });
    
        artMenu.open(player);
    }
    
    
    public static void openCustomBiomeEditor(Player player, CustomBiome customBiome) {
        BiomeDataContainer original = customBiome.getBiomeData();
        BiomeDataContainer newContainer = original.clone();
        
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
            ArtButton H = new ArtButton(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
            
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back-without-save")).build());
            ItemStack backWithSaveItem = new ItemBuilder(Material.WRITABLE_BOOK).name(SystemLanguage.getText("gui-back-with-save")).build();
            ItemMeta backWithSaveItemMeta = backWithSaveItem.getItemMeta();
            backWithSaveItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            backWithSaveItemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            MenuBackButton P = new MenuBackButton(backWithSaveItem);
            P.listener((event, menu) -> {
                original.write(newContainer);
                customBiome.applyChanges();
                HistoryData historyData = HistoryData.getHistoryData(VanillaSource.getPlugin().getArtGUI(), player);
                historyData.back();
            });
            
            return new ArtButton[]{
                    H, H, H, H, H, H, H, H, H,
                    H, V, V, V, V, V, V, V, H,
                    H, V, V, V, G, G, G, G, H,
                    H, H, H, H, H, H, H, H, H,
                    G, G, G, G, G, G, P, B, E,
            };
        });
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), SystemLanguage.getText("gui-edit-custom-biome", customBiome.getKey()));
    
        artMenu.asyncCreate(menu -> {
            ArtButton F = new RGBSelectButton(new ItemBuilder(Material.WHITE_STAINED_GLASS)
                    .name(SystemLanguage.getText("gui-fog-color"))
                    .lore(getRGBText(newContainer.fogColorRGB)).build(), rgb -> newContainer.fogColorRGB = rgb);
    
            ArtButton W = new RGBSelectButton(new ItemBuilder(Material.WATER_BUCKET)
                    .name(SystemLanguage.getText("gui-water-color"))
                    .lore(getRGBText(newContainer.waterColorRGB)).build(), rgb -> newContainer.waterColorRGB = rgb);
    
            ArtButton C = new RGBSelectButton(new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS)
                    .name(SystemLanguage.getText("gui-water-fog-color"))
                    .lore(getRGBText(newContainer.waterFogColorRGB)).build(), rgb -> newContainer.waterFogColorRGB = rgb);
    
            ArtButton S = new RGBSelectButton(new ItemBuilder(Material.FEATHER)
                    .name(SystemLanguage.getText("gui-sky-color"))
                    .lore(getRGBText(newContainer.skyColorRGB)).build(), rgb -> newContainer.skyColorRGB = rgb);
    
            ArtButton T = new ArtButton(
                    new ItemBuilder(newContainer.temperatureAttribute == BiomeDataContainer.TemperatureAttribute.NORMAL ? Material.GRASS_BLOCK : Material.ICE)
                            .name(SystemLanguage.getText("gui-temperature-attribute"))
                            .lore(SystemLanguage.getText("gui-current-selected", SystemLanguage.getText(newContainer.grassColorAttribute.name))).build()).listener((event, m) -> {
                openTemperatureAttributeGUI(player, SystemLanguage.getText("gui-temperature-attribute"), newContainer);
            });
    
            Material grassColorAttributeMaterial = Material.GRASS_BLOCK;
            switch (newContainer.grassColorAttribute) {
                case NORMAL:
                    break;
                case DARK_FOREST:
                    grassColorAttributeMaterial = Material.DARK_OAK_LEAVES;
                    break;
                case SWAMP:
                    grassColorAttributeMaterial = Material.CLAY;
                    break;
            }
            ArtButton K = new ArtButton(
                    new ItemBuilder(grassColorAttributeMaterial)
                            .name(SystemLanguage.getText("gui-grass-color-attribute"))
                            .lore(SystemLanguage.getText("gui-current-selected", SystemLanguage.getText(newContainer.grassColorAttribute.name))).build()).listener((event, m) ->
                    openGrassColorAttributeGUI(player, SystemLanguage.getText("gui-grass-color-attribute"), newContainer)
            );
    
    
            ArtButton O = new RGBSelectButton(new ItemBuilder(Material.OAK_LEAVES)
                    .name(SystemLanguage.getText("gui-foliage-color"))
                    .lore(getRGBText(newContainer.foliageColorRGB)).build(), true, rgb -> newContainer.foliageColorRGB = rgb);
    
            ArtButton A = new RGBSelectButton(new ItemBuilder(Material.GRASS_BLOCK)
                    .name(SystemLanguage.getText("gui-grass-block-color"))
                    .lore(getRGBText(newContainer.grassBlockColorRGB)).build(), true, rgb -> newContainer.grassBlockColorRGB = rgb);
    
            TextInputButton N = new TextInputButton(new ItemBuilder(Material.MUSIC_DISC_13)
                    .name(SystemLanguage.getText("gui-change-environment-sound"))
                    .lore(SystemLanguage.getText("gui-current-selected",
                            newContainer.music == null ? "NULL" : newContainer.music))
                    .build(),
                    SystemLanguage.getText("gui-change-environment-sound"), SystemLanguage.getText("gui-input-sound-name"));
            N.onInput((p, text) -> {
                newContainer.music = text;
                return null;
            });
    
            TextInputButton L = new TextInputButton(new ItemBuilder(Material.BONE_MEAL)
                    .name(SystemLanguage.getText("gui-edit-particle"))
                    .lore(SystemLanguage.getText("gui-current-selected", CustomBiome.getParticleDataString(newContainer))).build(),
                    SystemLanguage.getText("gui-edit-particle"), SystemLanguage.getText("gui-input-particle"));
            L.onInput((p, text) -> {
                try {
                    CustomBiome.setParticleData(newContainer, text);
                } catch (Exception e) {
                    player.sendMessage(SystemLanguage.getText("gui-invalid-particle", e.getMessage()));
                    e.printStackTrace();
                }
                return null;
            });
    
            menu.setButton(0, 10, F);
            menu.setButton(0, 11, W);
            menu.setButton(0, 12, C);
            menu.setButton(0, 13, S);
            menu.setButton(0, 14, T);
            menu.setButton(0, 15, K);
            menu.setButton(0, 16, O);
            menu.setButton(0, 19, A);
            menu.setButton(0, 20, N);
            menu.setButton(0, 21, L);
        });
    
        artMenu.open(player);
    }
    
    public static String getRGBText(Integer rgb) {
        if (rgb == null) {
            return "&7#NULL";
        }
        
        String rgbCode = "#" + String.format("%06x", rgb);
        return SystemLanguage.getText("gui-current-selected", ChatColor.of(rgbCode) + rgbCode);
    }
    
    public static void openSoundSelectGUI(Player player, String title, Consumer<Sound> onSelect) {
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
            
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
            
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
            
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
            
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
            
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
            
            
            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, I,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, B,
                    V, V, V, V, V, V, V, G, E,
            };
        });
        
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
        
        artMenu.asyncCreate(menu -> {
            for (Sound sound : Sound.values()) {
                menu.addButton(new ArtButton(new ItemBuilder(Material.MUSIC_DISC_13).name("&6&n" + sound.getKey())
                        .lore(SystemLanguage.getText("gui-right-click-to-play")).build())
                        .listener((event, m) -> {
                            if (event.getClick() == ClickType.RIGHT) {
                                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                            } else {
                                onSelect.accept(sound);
                                HistoryData historyData = HistoryData.getHistoryData(VanillaSource.getPlugin().getArtGUI(), player);
                                historyData.back();
                            }
                        }));
            }
        });
        
        artMenu.open(player);
    }
    
    
    public static void openTemperatureAttributeGUI(Player player, String title, BiomeDataContainer container) {
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
            
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
            
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
            
            return new ArtButton[]{
                    G, G, G, G, G, G, G, G, G,
                    G, V, V, V, V, V, V, V, B,
                    G, G, G, G, G, G, G, G, E,
            };
        });
        
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
        
        artMenu.asyncCreate(menu -> {
            menu.addButton(new ArtButton(new ItemBuilder(Material.GRASS_BLOCK)
                    .name(SystemLanguage.getText(BiomeDataContainer.TemperatureAttribute.NORMAL.name)).build())
                    .listener((event, menu1) -> {
                        container.temperatureAttribute = BiomeDataContainer.TemperatureAttribute.NORMAL;
                        HistoryData historyData = HistoryData.getHistoryData(menu.getArtMenu().getArtGUI(), player);
                        historyData.back();
                    }));
    
            menu.addButton(new ArtButton(new ItemBuilder(Material.ICE)
                    .name(SystemLanguage.getText(BiomeDataContainer.TemperatureAttribute.FROZEN.name)).build())
                    .listener((event, menu1) -> {
                        container.temperatureAttribute = BiomeDataContainer.TemperatureAttribute.FROZEN;
                        HistoryData historyData = HistoryData.getHistoryData(menu.getArtMenu().getArtGUI(), player);
                        historyData.back();
                    }));
        });
        
        artMenu.open(player);
    }
    
    
    public static void openGrassColorAttributeGUI(Player player, String title, BiomeDataContainer container) {
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
            
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
            
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
            
            return new ArtButton[]{
                    G, G, G, G, G, G, G, G, G,
                    G, V, V, V, V, V, V, V, B,
                    G, G, G, G, G, G, G, G, E,
            };
        });
        
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
        
        artMenu.asyncCreate(menu -> {
            menu.addButton(new ArtButton(new ItemBuilder(Material.GRASS_BLOCK)
                    .name(SystemLanguage.getText(BiomeDataContainer.GrassColorAttribute.NORMAL.name)).build())
                    .listener((event, menu1) -> {
                        container.grassColorAttribute = BiomeDataContainer.GrassColorAttribute.NORMAL;
                        HistoryData historyData = HistoryData.getHistoryData(menu.getArtMenu().getArtGUI(), player);
                        historyData.back();
                    }));
            
            menu.addButton(new ArtButton(new ItemBuilder(Material.DARK_OAK_LEAVES)
                    .name(SystemLanguage.getText(BiomeDataContainer.GrassColorAttribute.DARK_FOREST.name)).build())
                    .listener((event, menu1) -> {
                        container.grassColorAttribute = BiomeDataContainer.GrassColorAttribute.DARK_FOREST;
                        HistoryData historyData = HistoryData.getHistoryData(menu.getArtMenu().getArtGUI(), player);
                        historyData.back();
                    }));
    
            menu.addButton(new ArtButton(new ItemBuilder(Material.CLAY)
                    .name(SystemLanguage.getText(BiomeDataContainer.GrassColorAttribute.SWAMP.name)).build())
                    .listener((event, menu1) -> {
                        container.grassColorAttribute = BiomeDataContainer.GrassColorAttribute.SWAMP;
                        HistoryData historyData = HistoryData.getHistoryData(menu.getArtMenu().getArtGUI(), player);
                        historyData.back();
                    }));
        });
        
        artMenu.open(player);
    }
    
}

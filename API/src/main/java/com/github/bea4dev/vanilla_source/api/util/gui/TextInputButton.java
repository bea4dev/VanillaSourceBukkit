package com.github.bea4dev.vanilla_source.api.util.gui;

import com.github.bea4dev.artgui.button.ArtButton;
import com.github.bea4dev.artgui.menu.HistoryData;
import com.github.bea4dev.artgui.menu.MenuHistory;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;

public class TextInputButton extends ArtButton {
    
    private TextInputListener inputListener = null;
    
    public TextInputButton(ItemStack itemStack, String title, String defaultText) {
        super(itemStack);
        
        listener((event, menu) -> {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getWhoClicked();
    
            HistoryData historyData = HistoryData.getHistoryData(VanillaSourceAPI.getInstance().getArtGUI(), player);
            historyData.clearOnClose = false;
            
            new AnvilGUI.Builder()
                    .onClick((slot, stateSnapshot) -> {
                    if (inputListener != null) {
                        String incorrectText = inputListener.onInput(stateSnapshot.getPlayer(), stateSnapshot.getText());
                        if (incorrectText != null) {
                            if (incorrectText.equals("back")) {
                                Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), () -> {
                                    historyData.clearOnClose = true;
                                    historyData.back();
                                }, 1);
                                return AnvilGUI.Response.close();
                            } else if (incorrectText.equals("close")) {
                                return AnvilGUI.Response.close();
                            }
                            return AnvilGUI.Response.text(incorrectText);
                        }
                    }
                    
                    Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), () -> {
                        historyData.clearOnClose = true;
                        MenuHistory history = historyData.getCurrentMenu();
                        if (history == null){
                            return;
                        }
                        history.getArtMenu().open(player);
                    }, 1);
                    return AnvilGUI.Response.close();
                })
                .preventClose()
                .text(defaultText)
                .itemLeft(new ItemStack(Material.PAPER))
                .title(title)
                .plugin(VanillaSourceAPI.getInstance().getPlugin())
                .open(player);
        });
    }
    
    public TextInputButton onInput(TextInputListener inputListener) {
        this.inputListener = inputListener;
        return this;
    }
    
}

package com.github.bea4dev.vanilla_source.nms.v1_21_R7;

import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class WindowContentsPacketHandler implements IPacketHandler {
    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {
        if (packet instanceof ClientboundContainerSetContentPacket windowContentsPacket) {
            var items = new ArrayList<ItemStack>(windowContentsPacket.items().size());

            for (var item : windowContentsPacket.items()) {
                items.add(translateItem(item, enginePlayer.getBukkitPlayer()));
            }

            return new ClientboundContainerSetContentPacket(
                    windowContentsPacket.containerId(),
                    windowContentsPacket.stateId(),
                    items,
                    translateItem(windowContentsPacket.carriedItem(), enginePlayer.getBukkitPlayer())
            );
        } else if (packet instanceof ClientboundContainerSetSlotPacket setSlotPacket) {
            return new ClientboundContainerSetSlotPacket(
                    setSlotPacket.getContainerId(),
                    setSlotPacket.getStateId(),
                    setSlotPacket.getSlot(),
                    translateItem(setSlotPacket.getItem(), enginePlayer.getBukkitPlayer())
            );
        }

        return packet;
    }

    private ItemStack translateItem(ItemStack item, Player player) {
        var itemStack = CraftItemStack.asBukkitCopy(item);
        var meta = itemStack.getItemMeta();

        if (meta != null) {
            var displayName = meta.displayName();
            if (displayName != null) {
                meta.displayName(GlobalTranslator.render(displayName, player.locale()));
            }

            var lore = meta.lore();
            if (lore != null) {
                var newLore = new ArrayList<Component>(lore.size());
                for (var line : lore) {
                    newLore.add(GlobalTranslator.render(line, player.locale()));
                }
                meta.lore(newLore);
            }

            itemStack.setItemMeta(meta);
        }

        return CraftItemStack.asNMSCopy(itemStack);
    }
}

package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class WindowContentsPacketHandler implements IPacketHandler {
    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {
        if (packet instanceof ClientboundContainerSetContentPacket windowContentsPacket) {
            var items = windowContentsPacket.getItems();

            var index = 0;
            for (var item : items) {
                items.set(index, translateItem(item.copy(), enginePlayer.getBukkitPlayer()));
                index++;
            }

            return new ClientboundContainerSetContentPacket(
                    windowContentsPacket.getContainerId(),
                    windowContentsPacket.getStateId(),
                    (NonNullList<ItemStack>) items,
                    translateItem(windowContentsPacket.getCarriedItem().copy(), enginePlayer.getBukkitPlayer())
            );
        } else if (packet instanceof ClientboundContainerSetSlotPacket setSlotPacket) {
            return new ClientboundContainerSetSlotPacket(
                    setSlotPacket.getContainerId(),
                    setSlotPacket.getStateId(),
                    setSlotPacket.getSlot(),
                    translateItem(setSlotPacket.getItem().copy(), enginePlayer.getBukkitPlayer())
            );
        }

        return packet;
    }

    private net.minecraft.world.item.ItemStack translateItem(net.minecraft.world.item.ItemStack item, Player player) {
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

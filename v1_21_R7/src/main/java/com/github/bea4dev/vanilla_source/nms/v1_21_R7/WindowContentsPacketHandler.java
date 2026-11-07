package com.github.bea4dev.vanilla_source.nms.v1_21_R7;

import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

/**
 * アイテムやエンティティに含まれる Component.translatable(...) をプレイヤーの言語で描画してから送信する。
 */
public class WindowContentsPacketHandler implements IPacketHandler {
    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {
        return rewritePacket(packet, enginePlayer.getBukkitPlayer().locale());
    }

    @SuppressWarnings("unchecked")
    private Object rewritePacket(Object packet, Locale locale) {
        if (packet instanceof ClientboundContainerSetContentPacket windowContentsPacket) {
            var items = new ArrayList<ItemStack>(windowContentsPacket.items().size());

            for (var item : windowContentsPacket.items()) {
                items.add(translateItem(item, locale));
            }

            return new ClientboundContainerSetContentPacket(
                    windowContentsPacket.containerId(),
                    windowContentsPacket.stateId(),
                    items,
                    translateItem(windowContentsPacket.carriedItem(), locale)
            );
        } else if (packet instanceof ClientboundContainerSetSlotPacket setSlotPacket) {
            var item = setSlotPacket.getItem();
            var translated = translateItem(item, locale);
            if (translated == item) {
                return packet;
            }
            return new ClientboundContainerSetSlotPacket(
                    setSlotPacket.getContainerId(),
                    setSlotPacket.getStateId(),
                    setSlotPacket.getSlot(),
                    translated
            );
        } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventoryPacket) {
            // 1.21.2 以降、プレイヤーインベントリへの直接の更新はこのパケットで送られる
            var translated = translateItem(inventoryPacket.contents(), locale);
            if (translated == inventoryPacket.contents()) {
                return packet;
            }
            return new ClientboundSetPlayerInventoryPacket(inventoryPacket.slot(), translated);
        } else if (packet instanceof ClientboundSetCursorItemPacket cursorPacket) {
            var translated = translateItem(cursorPacket.contents(), locale);
            if (translated == cursorPacket.contents()) {
                return packet;
            }
            return new ClientboundSetCursorItemPacket(translated);
        } else if (packet instanceof ClientboundSetEntityDataPacket entityDataPacket) {
            // ドロップアイテム、額縁、ディスプレイエンティティのアイテムや名前
            var changed = false;
            var values = new ArrayList<SynchedEntityData.DataValue<?>>(entityDataPacket.packedItems().size());
            for (var value : entityDataPacket.packedItems()) {
                var translated = translateDataValue(value, locale);
                changed |= translated != value;
                values.add(translated);
            }
            return changed ? new ClientboundSetEntityDataPacket(entityDataPacket.id(), values) : packet;
        } else if (packet instanceof ClientboundBundlePacket bundlePacket) {
            // エンティティのスポーン時のデータはバンドルにまとめて送られる
            var changed = false;
            var packets = new ArrayList<Packet<? super ClientGamePacketListener>>();
            for (var subPacket : bundlePacket.subPackets()) {
                var translated = (Packet<? super ClientGamePacketListener>) rewritePacket(subPacket, locale);
                changed |= translated != subPacket;
                packets.add(translated);
            }
            return changed ? new ClientboundBundlePacket(packets) : packet;
        }

        return packet;
    }

    @SuppressWarnings("unchecked")
    private SynchedEntityData.DataValue<?> translateDataValue(SynchedEntityData.DataValue<?> dataValue, Locale locale) {
        var value = dataValue.value();
        Object translated = value;

        if (value instanceof ItemStack item) {
            translated = translateItem(item, locale);
        } else if (value instanceof Component component) {
            translated = translateComponent(component, locale);
        } else if (value instanceof Optional<?> optional && optional.isPresent() && optional.get() instanceof Component component) {
            var translatedComponent = translateComponent(component, locale);
            if (translatedComponent != component) {
                translated = Optional.of(translatedComponent);
            }
        }

        if (translated == value) {
            return dataValue;
        }
        return new SynchedEntityData.DataValue<>(dataValue.id(), (EntityDataSerializer<Object>) dataValue.serializer(), translated);
    }

    /**
     * 翻訳が必要なときだけコピーを作る。必要なければ引数をそのまま返す。
     */
    private ItemStack translateItem(ItemStack item, Locale locale) {
        if (item.isEmpty()) {
            return item;
        }

        // 既定値 (バニラのアイテム名など) はクライアント側で翻訳されるので、変更された部分だけを見る
        var patch = item.getComponentsPatch();
        var customName = translatePatchedComponent(patch.get(DataComponents.CUSTOM_NAME), locale);
        var itemName = translatePatchedComponent(patch.get(DataComponents.ITEM_NAME), locale);
        var lore = translateLore(patch.get(DataComponents.LORE), locale);

        if (customName == null && itemName == null && lore == null) {
            return item;
        }

        var copy = item.copy();
        setIfPresent(copy, DataComponents.CUSTOM_NAME, customName);
        setIfPresent(copy, DataComponents.ITEM_NAME, itemName);
        setIfPresent(copy, DataComponents.LORE, lore);
        return copy;
    }

    private static <T> void setIfPresent(ItemStack item, DataComponentType<T> type, T value) {
        if (value != null) {
            item.set(type, value);
        }
    }

    /**
     * 変更がなければ null を返す
     */
    private Component translatePatchedComponent(Optional<? extends Component> patched, Locale locale) {
        if (patched == null || patched.isEmpty()) {
            return null;
        }
        var component = patched.get();
        var translated = translateComponent(component, locale);
        return translated == component ? null : translated;
    }

    /**
     * 変更がなければ null を返す
     */
    private ItemLore translateLore(Optional<? extends ItemLore> patched, Locale locale) {
        if (patched == null || patched.isEmpty()) {
            return null;
        }
        var lore = patched.get();
        var changed = false;
        var lines = new ArrayList<Component>(lore.lines().size());
        for (var line : lore.lines()) {
            var translated = translateComponent(line, locale);
            changed |= translated != line;
            lines.add(translated);
        }
        return changed ? new ItemLore(lines) : null;
    }

    private Component translateComponent(Component component, Locale locale) {
        if (!containsTranslatable(component)) {
            return component;
        }
        var adventure = PaperAdventure.asAdventure(component);
        var rendered = GlobalTranslator.render(adventure, locale);
        if (rendered.equals(adventure)) {
            return component;
        }
        return PaperAdventure.asVanilla(rendered);
    }

    private static boolean containsTranslatable(Component component) {
        if (component.getContents() instanceof TranslatableContents) {
            return true;
        }
        for (var sibling : component.getSiblings()) {
            if (containsTranslatable(sibling)) {
                return true;
            }
        }
        return false;
    }
}

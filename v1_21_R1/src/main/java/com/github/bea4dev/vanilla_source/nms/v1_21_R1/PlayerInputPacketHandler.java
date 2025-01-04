package com.github.bea4dev.vanilla_source.nms.v1_21_R1;

import com.github.bea4dev.vanilla_source.api.nms.IPacketHandler;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.text.TextBoxManager;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;

public class PlayerInputPacketHandler implements IPacketHandler {
    @Override
    public Object rewrite(Object packet, EnginePlayer enginePlayer, boolean cacheSetting) {
        var inputPacket = (ServerboundPlayerInputPacket) packet;

        TextBoxManager.onVehicleSneak(enginePlayer.getBukkitPlayer(), inputPacket.isShiftKeyDown());

        return packet;
    }
}

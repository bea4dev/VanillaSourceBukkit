package com.github.bea4dev.vanilla_source.api.nms;

import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;

public interface IPacketHandler {

    Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting);

}

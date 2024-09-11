package com.github.bea4dev.vanilla_source.api.item;

import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class EngineItemInstance {

    protected final UUID uuid = UUID.randomUUID();

    protected @Nullable EnginePlayer holder = null;

    public UUID getUUID() {return uuid;}

    public abstract void onLeftClick();

    public abstract void onRightClick();

    public abstract void create();

}

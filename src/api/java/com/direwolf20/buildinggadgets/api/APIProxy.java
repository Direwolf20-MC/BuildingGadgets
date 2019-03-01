package com.direwolf20.buildinggadgets.api;


import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Objects;

public final class APIProxy {
    private final IEventBus modEventbus;
    private final IEventBus forgeEventbus;

    public APIProxy(IEventBus modEventbus, IEventBus forgeEventbus) {
        this.modEventbus = Objects.requireNonNull(modEventbus);
        this.forgeEventbus = Objects.requireNonNull(forgeEventbus);
    }

    public void onSetup() {

    }

    public void onLoadComplete() {

    }
}

package com.direwolf20.buildinggadgets.api;


import com.direwolf20.buildinggadgets.api.abstraction.IApiConfig;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Objects;

public final class APIProxy {
    private final IEventBus modEventbus;
    private final IEventBus forgeEventbus;
    private final IApiConfig config;

    public APIProxy(IEventBus modEventbus, IEventBus forgeEventbus, IApiConfig config) {
        this.modEventbus = Objects.requireNonNull(modEventbus);
        this.forgeEventbus = Objects.requireNonNull(forgeEventbus);
        this.config = config;
    }

    public void onSetup() {

    }

    public void onLoadComplete() {

    }

    public IApiConfig getConfig() {
        return config;
    }
}

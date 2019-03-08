package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.api.abstraction.IApiConfig;
import com.google.common.base.Preconditions;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum APIProxy {
    INSTANCE;
    public static final Logger LOG = LogManager.getLogger();
    private IEventBus modEventbus = null;
    private IEventBus forgeEventbus = null;
    private IApiConfig config = null;

    public APIProxy onCreate(IEventBus modEventbus, IEventBus forgeEventbus, IApiConfig config) {
        Preconditions.checkState(this.modEventbus == null && this.forgeEventbus == null && this.config == null);
        this.modEventbus = modEventbus;
        this.forgeEventbus = forgeEventbus;
        this.config = config;
        return this;
    }

    public void onSetup() {

    }

    public void onLoadComplete() {

    }

    public IApiConfig getConfig() {
        return config;
    }
}

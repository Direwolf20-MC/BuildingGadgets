package com.direwolf20.buildinggadgets.common.concurrent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.BooleanSupplier;

public final class ServerTickingScheduler {
    private final BooleanSupplier runnable;

    public ServerTickingScheduler(BooleanSupplier runnable) {
        this.runnable = runnable;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tick(ServerTickEvent event) {
        if (event.phase != Phase.START)
            return;
        if (! runnable.getAsBoolean())
            MinecraftForge.EVENT_BUS.unregister(this);
    }
}

package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;

public final class ServerTickingScheduler {
    public static void runTicked(BooleanSupplier runUntilFalse) {
        new ServerTickingScheduler(runUntilFalse, EnumSet.of(Phase.START));
    }

    public static void runTickedAtEnd(BooleanSupplier runUntilFalse) {
        new ServerTickingScheduler(runUntilFalse, EnumSet.of(Phase.END));
    }

    public static void runTickedStartAndEnd(BooleanSupplier runUntilFalse) {
        new ServerTickingScheduler(runUntilFalse, EnumSet.allOf(Phase.class));
    }

    public static void runOnServerOnce(Runnable runnable) {
        runTickedStartAndEnd(() -> {
            runnable.run();
            return false;
        });
    }

    private final BooleanSupplier runnable;
    private final EnumSet<Phase> phases;

    private ServerTickingScheduler(BooleanSupplier runnable, EnumSet<Phase> phases) {
        this.runnable = runnable;
        MinecraftForge.EVENT_BUS.register(this);
        this.phases = phases;
    }

    @SubscribeEvent
    public void tick(ServerTickEvent event) {
        if (! phases.contains(event.phase))
            return;
        if (! runnable.getAsBoolean())
            MinecraftForge.EVENT_BUS.unregister(this);
    }
}

package com.direwolf20.buildinggadgets.common.concurrent;

import net.minecraftforge.fml.DeferredWorkQueue;

import java.util.function.BooleanSupplier;

public final class ServerTickingScheduler {
    public static void runTicked(BooleanSupplier runUntilFalse) {
        DeferredWorkQueue.runLater(() -> {
            boolean res = runUntilFalse.getAsBoolean();
            if (res)
                runTicked(runUntilFalse);
        });
    }

    public static void runOnServerOnce(Runnable runnable) {
        DeferredWorkQueue.runLater(runnable);
    }
}

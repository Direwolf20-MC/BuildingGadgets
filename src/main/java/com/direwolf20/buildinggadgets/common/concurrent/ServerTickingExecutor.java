package com.direwolf20.buildinggadgets.common.concurrent;

import java.util.concurrent.Executor;

public enum ServerTickingExecutor implements Executor {
    INSTANCE;

    @Override
    public void execute(Runnable command) {
        new ServerTickingScheduler(() -> {
            command.run();
            return false;
        });
    }
}

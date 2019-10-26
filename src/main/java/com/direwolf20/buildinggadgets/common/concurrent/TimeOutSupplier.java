package com.direwolf20.buildinggadgets.common.concurrent;

import java.util.function.BooleanSupplier;

public abstract class TimeOutSupplier implements BooleanSupplier {
    private int count;
    private final int timeout;

    public TimeOutSupplier(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean getAsBoolean() {
        if (count > timeout) {
            onTimeout();
            return false;
        } else
            ++ count;
        return run();
    }

    protected abstract boolean run();

    protected abstract void onTimeout();
}

package com.direwolf20.buildinggadgets.common.concurrent;

import java.util.function.BooleanSupplier;

public abstract class SteppedScheduler implements BooleanSupplier {
    private final int steps;
    private boolean finished;

    public SteppedScheduler(int steps) {
        this.steps = steps;
        this.finished = false;
    }

    @Override
    public boolean getAsBoolean() {
        if (finished)
            return false;
        for (int i = 0; advance() && i < steps - 1; ++ i)
            ;
        boolean res = advance();
        if (! res) {
            this.finished = true;
            onFinish();
        }
        return res;
    }

    protected abstract boolean advance();

    protected abstract void onFinish();
}

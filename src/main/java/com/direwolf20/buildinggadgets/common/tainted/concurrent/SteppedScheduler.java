package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import java.util.function.BooleanSupplier;

public abstract class SteppedScheduler implements BooleanSupplier {
    protected enum StepResult {
        SUCCESS,
        FAILURE,
        END;

        public static StepResult ofBoolean(boolean b) {
            return b ? SUCCESS : END;
        }
    }
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
        for (int i = 0; advance() != StepResult.END && i < steps - 1; ++ i)
            ;
        boolean res = advance() != StepResult.END;
        if (! res) {
            this.finished = true;
            onFinish();
        }
        return res;
    }

    protected abstract StepResult advance();

    protected abstract void onFinish();
}

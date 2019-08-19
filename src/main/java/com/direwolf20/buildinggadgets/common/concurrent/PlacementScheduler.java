package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class PlacementScheduler implements BooleanSupplier {
    public static void schedulePlacement(Consumer<PlacementTarget> consumer, IBuildView view, int steps) {
        Preconditions.checkArgument(steps > 0);
        ServerTickingScheduler.runTicked(new PlacementScheduler(
                Objects.requireNonNull(consumer),
                Objects.requireNonNull(view),
                steps, true));
    }

    public static void schedulePlacementNoClose(Consumer<PlacementTarget> consumer, IBuildView view, int steps) {
        Preconditions.checkArgument(steps > 0);
        ServerTickingScheduler.runTicked(new PlacementScheduler(
                Objects.requireNonNull(consumer),
                Objects.requireNonNull(view),
                steps, false));
    }

    private final IBuildView view;
    private final int steps;
    private Spliterator<PlacementTarget> spliterator;
    private Consumer<PlacementTarget> consumer;
    private boolean finished;
    private boolean close;

    private PlacementScheduler(Consumer<PlacementTarget> consumer, IBuildView view, int steps, boolean close) {
        this.consumer = consumer;
        this.view = view;
        this.steps = steps;
        this.spliterator = view.spliterator();
        this.finished = false;
        this.close = close;
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
            if (close) {
                try {
                    view.close();
                } catch (TemplateException e) {
                    throw new RuntimeException("Attempt to close Template-IBuildView failed!", e);
                }
            }
        }
        return res;
    }

    private boolean advance() {
        return spliterator.tryAdvance(consumer);
    }
}

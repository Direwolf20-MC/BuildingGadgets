package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class PlacementScheduler extends SteppedScheduler {
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
    private Spliterator<PlacementTarget> spliterator;
    private Consumer<PlacementTarget> consumer;
    private boolean close;

    private PlacementScheduler(Consumer<PlacementTarget> consumer, IBuildView view, int steps, boolean close) {
        super(steps);
        this.consumer = consumer;
        this.view = view;
        this.spliterator = view.spliterator();
        this.close = close;
    }
    @Override
    protected void onFinish() {
        if (close) {
            try {
                view.close();
            } catch (TemplateException e) {
                throw new RuntimeException("Attempt to close Template-IBuildView failed!", e);
            }
        }
    }

    @Override
    protected boolean advance() {
        return spliterator.tryAdvance(consumer);
    }
}

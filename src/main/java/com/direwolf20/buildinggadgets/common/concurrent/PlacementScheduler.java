package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.save.Undo.Builder;
import com.direwolf20.buildinggadgets.common.util.tools.building.PlacementChecker;
import com.direwolf20.buildinggadgets.common.util.tools.building.PlacementChecker.CheckResult;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class PlacementScheduler extends SteppedScheduler {
    public static PlacementScheduler schedulePlacement(IBuildView view, PlacementChecker checker, int steps) {
        Preconditions.checkArgument(steps > 0);
        PlacementScheduler res = new PlacementScheduler(
                Objects.requireNonNull(view),
                Objects.requireNonNull(checker),
                steps, true);
        ServerTickingScheduler.runTicked(res);
        return res;
    }

    public static PlacementScheduler schedulePlacementNoClose(IBuildView view, PlacementChecker checker, int steps) {
        Preconditions.checkArgument(steps > 0);
        PlacementScheduler res = new PlacementScheduler(
                Objects.requireNonNull(view),
                Objects.requireNonNull(checker),
                steps, false);
        ServerTickingScheduler.runTicked(res);
        return res;
    }
    private final IBuildView view;
    private final Spliterator<PlacementTarget> spliterator;
    private final PlacementChecker checker;
    private final boolean close;
    private boolean lastWasSuccess;
    private Consumer<PlacementScheduler> finisher;
    private Undo.Builder undoBuilder;

    private PlacementScheduler(IBuildView view, PlacementChecker checker, int steps, boolean close) {
        super(steps);
        this.checker = checker;
        this.view = view;
        this.spliterator = view.spliterator();
        this.close = close;
        this.undoBuilder = Undo.builder();
        this.finisher = p -> {};
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
        finisher.accept(this);
    }

    @Override
    protected StepResult advance() {
        if (! spliterator.tryAdvance(this::checkTarget))
            return StepResult.END;
        return lastWasSuccess ? StepResult.SUCCESS : StepResult.FAILURE;
    }

    public Builder getUndoBuilder() {
        return undoBuilder;
    }

    public PlacementScheduler withFinisher(Consumer<PlacementScheduler> runnable) {
        this.finisher = Objects.requireNonNull(runnable);
        return this;
    }

    private void checkTarget(PlacementTarget target) {
        CheckResult res = checker.checkPositionWithResult(view.getContext(), target, false);
        lastWasSuccess = res.isSuccess();
        if (lastWasSuccess) {
            undoBuilder.record(view.getContext().getWorld(), target.getPos(), res.getMatch().getChosenOption(), res.getInsertedItems());
            EffectBlock.spawnEffectBlock(view.getContext(), target, Mode.PLACE, res.isUsingPaste());
        }
    }
}

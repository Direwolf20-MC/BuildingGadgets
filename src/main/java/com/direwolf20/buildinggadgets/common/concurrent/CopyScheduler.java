package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class CopyScheduler extends SteppedScheduler {
    public static void scheduleCopy(Consumer<ImmutableMap<BlockPos, BlockData>> finisher, Iterable<PlacementTarget> worldView, int steps) {
        Preconditions.checkArgument(steps > 0);
        ServerTickingScheduler.runTicked(new CopyScheduler(
                Objects.requireNonNull(finisher),
                Objects.requireNonNull(worldView),
                steps
        ));
    }

    private final Consumer<ImmutableMap<BlockPos, BlockData>> finisher;
    private final Spliterator<PlacementTarget> targets;
    private final ImmutableMap.Builder<BlockPos, BlockData> builder;

    private CopyScheduler(Consumer<ImmutableMap<BlockPos, BlockData>> finisher, Iterable<PlacementTarget> worldView, int steps) {
        super(steps);
        this.finisher = finisher;
        this.targets = worldView.spliterator();
        builder = ImmutableMap.builder();
    }

    @Override
    protected boolean advance() {
        return targets.tryAdvance(t -> builder.put(t.getPos(), t.getData()));
    }

    @Override
    protected void onFinish() {
        finisher.accept(builder.build());
    }
}

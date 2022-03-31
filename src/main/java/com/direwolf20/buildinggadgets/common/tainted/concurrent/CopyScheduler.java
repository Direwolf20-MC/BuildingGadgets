package com.direwolf20.buildinggadgets.common.tainted.concurrent;

import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;

public final class CopyScheduler extends SteppedScheduler {
    public static void scheduleCopy(BiConsumer<ImmutableMap<BlockPos, BlockData>, Region> finisher, IBuildView worldView, int steps) {
        Preconditions.checkArgument(steps > 0);
        ServerTickingScheduler.runTicked(new CopyScheduler(
                Objects.requireNonNull(finisher),
                Objects.requireNonNull(worldView),
                steps
        ));
    }

    private final BiConsumer<ImmutableMap<BlockPos, BlockData>, Region> finisher;
    private final Spliterator<PlacementTarget> targets;
    private final ImmutableMap.Builder<BlockPos, BlockData> builder;
    private Region.Builder regionBuilder;
    private final BuildContext context;

    private CopyScheduler(BiConsumer<ImmutableMap<BlockPos, BlockData>, Region> finisher, IBuildView worldView, int steps) {
        super(steps);
        this.finisher = finisher;
        this.targets = worldView.spliterator();
        this.builder = ImmutableMap.builder();
        this.context = worldView.getContext();
        this.regionBuilder = null;
    }

    @Override
    protected StepResult advance() {
        return StepResult.ofBoolean(targets.tryAdvance(t -> {
            if (!t.getData().getState().isAir() && ((GadgetCopyPaste) OurItems.COPY_PASTE_GADGET_ITEM.get()).isAllowedBlock(t.getData().getState())) {
                builder.put(t.getPos(), t.getData());
                if (regionBuilder == null)
                    regionBuilder = Region.enclosingBuilder();
                regionBuilder.enclose(t.getPos());
            }
        }));
    }

    @Override
    protected void onFinish() {
        finisher.accept(builder.build(), regionBuilder != null ? regionBuilder.build() : Region.singleZero());
    }
}

package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.save.Undo.BlockInfo;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.google.common.base.Preconditions;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

public final class UndoScheduler extends SteppedScheduler {
    public static UndoScheduler scheduleUndo(Undo undo, IItemIndex index, IBuildContext context, int steps) {
        Preconditions.checkArgument(steps > 0);
        UndoScheduler res = new UndoScheduler(
                Objects.requireNonNull(undo),
                Objects.requireNonNull(index),
                Objects.requireNonNull(context),
                steps);
        ServerTickingScheduler.runTicked(res);
        return res;
    }

    private final Spliterator<Map.Entry<BlockPos, BlockInfo>> spliterator;
    private boolean lastWasSuccess;
    private final IBuildContext context;
    private final IItemIndex index;
    private final BlockItemUseContext useContext;

    private UndoScheduler(Undo undo, IItemIndex index, IBuildContext context, int steps) {
        super(steps);
        assert context.getBuildingPlayer() != null;
        assert ! context.getUsedStack().isEmpty();
        this.useContext = new BlockItemUseContext(new ItemUseContext(context.getBuildingPlayer(), Hand.MAIN_HAND,
                VectorHelper.getLookingAt(context.getBuildingPlayer(), context.getUsedStack())));
        this.spliterator = undo.getUndoData().entrySet().spliterator();
        this.index = index;
        this.context = context;
    }

    @Override
    protected StepResult advance() {
        if (! spliterator.tryAdvance(this::undoBlock))
            return StepResult.END;
        return lastWasSuccess ? StepResult.SUCCESS : StepResult.FAILURE;
    }

    private void undoBlock(Map.Entry<BlockPos, BlockInfo> entry) {
        MatchResult matchResult = index.tryMatch(entry.getValue().getProducedItems());
        lastWasSuccess = matchResult.isSuccess();
        if (lastWasSuccess) {
            index.applyMatch(matchResult);
            index.insert(entry.getValue().getUsedItems());
            EffectBlock.spawnEffectBlock(context, new PlacementTarget(entry.getKey(), entry.getValue().getData()), Mode.REPLACE, false);
        }
    }

    @Override
    protected void onFinish() {

    }
}

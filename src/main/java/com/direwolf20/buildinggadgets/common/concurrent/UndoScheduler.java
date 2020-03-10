package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.save.Undo.BlockInfo;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

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
                steps
        );

        ServerTickingScheduler.runTicked(res);
        return res;
    }

    private final Spliterator<Map.Entry<BlockPos, BlockInfo>> spliterator;
    private boolean lastWasSuccess;
    private final IBuildContext context;
    private final IItemIndex index;

    private UndoScheduler(Undo undo, IItemIndex index, IBuildContext context, int steps) {
        super(steps);
        assert context.getBuildingPlayer() != null;
        assert ! context.getUsedStack().isEmpty();

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
        //if the block that was placed is no longer there, we should not undo anything
        BlockState state = context.getWorld().getBlockState(entry.getKey());
        TileEntity te = context.getWorld().getTileEntity(entry.getKey());
        BlockData data;
        if (state.getBlock() == OurBlocks.constructionBlock && te instanceof ConstructionBlockTileEntity) {
            data = ((ConstructionBlockTileEntity) te).getConstructionBlockData();
        } else
            data = TileSupport.createBlockData(state, te);
        if (! data.equals(entry.getValue().getPlacedData())) {
            lastWasSuccess = false;
            return;
        }
        if (! state.isAir(context.getWorld(), entry.getKey())) {
            BreakEvent event = new BreakEvent(context.getWorld().getWorld(), entry.getKey(), state, context.getBuildingPlayer());
            if (MinecraftForge.EVENT_BUS.post(event)) {
                lastWasSuccess = false;
                return;
            }
        }
        MatchResult matchResult = index.tryMatch(entry.getValue().getProducedItems());
        lastWasSuccess = matchResult.isSuccess();
        if (lastWasSuccess) {
            index.applyMatch(matchResult);
            index.insert(entry.getValue().getUsedItems());
            EffectBlock.spawnUndoBlock(context, new PlacementTarget(entry.getKey(), entry.getValue().getRecordedData()));
        }
    }

    @Override
    protected void onFinish() {

    }
}

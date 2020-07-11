package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.construction.*;
import com.direwolf20.buildinggadgets.common.construction.modes.*;
import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class BuildingGadget extends Gadget {
    private static final List<Mode> MODES = Arrays.asList(
        new BuildToMeMode(),
        new VerticalColumnMode(false),
        new HorizontalColumnMode(false),
        new VerticalWallMode(),
        new HorizontalWallMode(),
        new StairMode(),
        new GridMode(false),
        new SurfaceMode(false)
    );

    public BuildingGadget() {
        super();
    }

    @Override
    public boolean action(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace) {
        // If we have a block to build with, attempt make a collection of blocks from the current mode and build them
        Optional<BlockState> state = this.getBlock(gadget);
        state.ifPresent(blockState -> this.build(worldIn, playerIn, gadget, rayTrace, blockState));

        return state.isPresent();
    }

    // TODO: 11/07/2020 Cleanup :D
    protected void build(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace, BlockState state) {
        if( rayTrace == null ) {
            return;
        }

        // First, get the gadgets mode
        Mode mode = this.getMode(gadget);
        List<BlockPos> blockCollection = mode.getCollection(playerIn, new ModeUseContext(worldIn, state, rayTrace.getPos(), gadget, rayTrace.getFace(), true));

        // Build and store to the undo worldStore
        UndoWorldStore store = UndoWorldStore.get(worldIn);
        List<UndoBit> bits = new ArrayList<>();
        for (BlockPos e : blockCollection) {
            if (state.isValidPosition(worldIn, e) && worldIn.setBlockState(e, state)) {
                bits.add(new UndoBit(e, state, worldIn.getDimension().getType()));
            }
        }

        // if there was no blocks places, do not store the undo
        if (bits.size() == 0) {
            return;
        }

        playerIn.sendStatusMessage(LangHelper.compMessage("message", "build-successful"), true);

        UUID uuid = UUID.randomUUID();
        if (this.pushUndo(gadget, uuid, worldIn.getDimension().getType())) {
            store.push(uuid, bits);
        } else {
            playerIn.sendStatusMessage(LangHelper.compMessage("message", "undo-save-failure"), true);
        }
    }

    /**
     * Handles selecting the block for the Gadget. The selected block is used to build with when building.
     *
     * @param gadget    The held gadget
     * @param rayTrace  Trace result
     * @return          ActionResult
     */
    @Override
    public ActionResult<ItemStack> sneakingAction(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace) {
        if( rayTrace == null ) {
            playerIn.sendStatusMessage(LangHelper.compMessage("message", "no-block-selected"), true);
            return ActionResult.resultFail(gadget);
        }

        BlockState state = worldIn.getBlockState(rayTrace.getPos());
        if (BlockAuthority.allowed(state)) {
            this.setBlock(gadget, StateAuthority.pipe(state));
            playerIn.sendStatusMessage(LangHelper.compMessage("message", "block-selected", state.getBlock().getNameTextComponent().getFormattedText()), true);
            return ActionResult.resultSuccess(gadget);
        }

        playerIn.sendStatusMessage(LangHelper.compMessage("message", "block-selection-banned", state.getBlock().getNameTextComponent().getFormattedText()), true);
        return ActionResult.resultFail(gadget);
    }

    /**
     * What the undo will actually do :D
     */
    @Override
    public void undoAction(UndoWorldStore store, UUID uuid, ItemStack gadget, World world, PlayerEntity player) {
        List<UndoBit> bits = store.getUndoStack().get(uuid);

        if (bits == null) {
            BuildingGadgets.LOGGER.debug("Failed to get undo data :( " + uuid.toString());
            player.sendStatusMessage(LangHelper.compMessage("message", "undo-fetch-failure"), true);
            return;
        }

        for (UndoBit bit : bits) {
            // Don't undo blocks we didn't place / that were replaced
            if (world.getBlockState(bit.getPos()).getBlock() != bit.getState().getBlock()) {
                continue;
            }

            world.setBlockState(bit.getPos(), Blocks.AIR.getDefaultState());
        }

        store.pop(uuid);
    }

    @Override
    public List<Mode> getModes() {
        return MODES;
    }
}

package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.construction.UndoBit;
import com.direwolf20.buildinggadgets.common.construction.UndoStack;
import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import com.direwolf20.buildinggadgets.common.helpers.LookingHelper;
import com.direwolf20.buildinggadgets.common.modes.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.codec.language.bm.Lang;

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
    public void action() {

    }

    /**
     * Fetches the last UUID stored on the gadget, validates it then reverts the "bits"
     */
    @Override
    public void undo(ItemStack gadget, World world, PlayerEntity player) {
        UndoWorldStore store = UndoWorldStore.get(world);
        UndoStack undoStack = new UndoStack(gadget);

        Optional<UUID> uuid = undoStack.pollBit(world.getDimension().getType());

        if (!uuid.isPresent()) {
            // Not perfect but it's alright for now :D
            List<UUID> bitsByDimension = undoStack.getBitsByDimension(world.getDimension().getType());
            player.sendStatusMessage(LangHelper.compMessage("message", bitsByDimension.size() == 0 ? "undo-store-empty" : "undo-fetch-failure"), true);
        }

        uuid.ifPresent(key -> {
            List<UndoBit> bits = store.getUndoStack().get(key);

            if (bits == null) {
                BuildingGadgets.LOGGER.debug("Failed to get undo data :( " + uuid.toString());
                return;
            }

            // TODO: 10/07/2020 Add checking
            for (UndoBit bit : bits) {
                world.setBlockState(bit.getPos(), Blocks.AIR.getDefaultState());
            }

            store.getUndoStack().remove(key);
            store.markDirty();
        });
    }

    @Override
    public List<Mode> getModes() {
        return MODES;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if( worldIn.isRemote ) {
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }

        ItemStack gadget = playerIn.getHeldItem(handIn);
        BlockRayTraceResult rayTrace = LookingHelper.getBlockResult(playerIn, false);
        if( playerIn.isSneaking() ) {
            return this.selectBlock(worldIn, rayTrace, playerIn, gadget);
        }

        // If we have a block to build with, attempt make a collection of blocks from the current mode and build them
        Optional<BlockState> state = this.getBlock(gadget);
        state.ifPresent(blockState -> this.collectAndBuild(worldIn, rayTrace, playerIn, gadget, blockState));

        if( state.isPresent() )
            return ActionResult.resultPass(gadget);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    /**
     * Handles selecting the block for the Gadget. The selected block is used to build with when building.
     *
     * @param trace     Trace result
     * @param gadget    The held gadget
     * @return          ActionResult
     */
    private ActionResult<ItemStack> selectBlock(World world, @Nullable BlockRayTraceResult trace, PlayerEntity playerIn, ItemStack gadget) {
        if( trace == null ) {
            playerIn.sendStatusMessage(LangHelper.compMessage("message", "no-block-selected"), true);
            return ActionResult.resultFail(gadget);
        }

        BlockState state = world.getBlockState(trace.getPos());
        System.out.println(state);
        this.setBlock(gadget, state);

        playerIn.sendStatusMessage(LangHelper.compMessage("message", "block-selected", state.getBlock().getNameTextComponent().getFormattedText()), true);
        return ActionResult.resultSuccess(gadget);
    }

    protected void collectAndBuild(World worldIn, @Nullable BlockRayTraceResult trace, PlayerEntity playerIn, ItemStack gadget, BlockState state) {
        if( trace == null ) {
            return;
        }

        // First, get the gadgets mode
        Mode mode = this.getMode(gadget);
        List<BlockPos> blockCollection = mode.getCollection(playerIn, new ModeUseContext(worldIn, state, trace.getPos(), gadget, trace.getFace(), true));

        // Build and store to the undo worldStore
        UndoWorldStore store = UndoWorldStore.get(worldIn);
        List<UndoBit> bits = new ArrayList<>();
        for (BlockPos e : blockCollection) {
            if (worldIn.setBlockState(e, state)) {
                bits.add(new UndoBit(e, state, worldIn.getDimension().getType()));
            }
        }

        UUID uuid = UUID.randomUUID();
        if (this.pushUndo(gadget, uuid, worldIn.getDimension().getType())) {
            store.push(uuid, bits);
        } else {
            playerIn.sendStatusMessage(LangHelper.compMessage("message", "undo-save-failure"), true);
        }
    }
}

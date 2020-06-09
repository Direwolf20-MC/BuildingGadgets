package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import com.direwolf20.buildinggadgets.common.modes.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    protected List<Mode> getModes() {
        return MODES;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if( worldIn.isRemote ) {
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }

        ItemStack gadget = playerIn.getHeldItem(handIn);
        if( playerIn.isSneaking() ) {
            return this.selectBlock(worldIn, playerIn, gadget);
        }

        // If we have a block to build with, attempt make a collection of blocks from the current mode and build them
        Optional<BlockState> state = this.getBlock(gadget);
        state.ifPresent(blockState -> this.collectAndBuild(worldIn, playerIn, gadget, blockState));

        if( state.isPresent() )
            return ActionResult.resultPass(gadget);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
        String newMode = this.rotateModes(itemstack);
        player.sendStatusMessage(new StringTextComponent("Mode changed to: " + newMode), true);
        return super.onBlockStartBreak(itemstack, pos, player);
    }

    /**
     * Handles selecting the block for the Gadget. The selected block is used to build with when building.
     *
     * @param gadget    The held gadget
     * @return          ActionResult
     */
    private ActionResult<ItemStack> selectBlock(World worldIn, PlayerEntity playerIn, ItemStack gadget) {
        Optional<BlockState> lookingAt = Gadget.getLookingAt(worldIn, playerIn, 20);
        lookingAt.ifPresent(blockState -> this.setBlock(gadget, blockState));

        if( !lookingAt.isPresent() ) {
            playerIn.sendStatusMessage(LangHelper.compMessage("message.no-block-selected"), true);
            return ActionResult.resultSuccess(gadget);
        }

        playerIn.sendStatusMessage(LangHelper.compMessage("message.block-selected", lookingAt.get().getBlock().getNameTextComponent().getFormattedText()), true);
        return ActionResult.resultFail(gadget);
    }

    private void collectAndBuild(World worldIn, PlayerEntity playerIn, ItemStack gadget, BlockState state) {
        // First, get the gadgets mode

    }
}

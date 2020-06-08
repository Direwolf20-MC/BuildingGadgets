package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;

public class BuildingGadget extends Gadget {
    public BuildingGadget() {
        super();

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

package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Optional;

public class BuildingGadget extends Gadget {
    public enum Modes {
        BUILD_TO_ME()
    }

    public BuildingGadget() {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if( worldIn.isRemote ) {
            return super.onItemRightClick(worldIn, playerIn, handIn);
        }

        ItemStack gadget = playerIn.getHeldItem(handIn);

        // Todo: add config range here
        if( playerIn.isSneaking() ) {
            Optional<BlockState> lookingAt = Gadget.getLookingAt(worldIn, playerIn, 20);

            lookingAt.ifPresent(blockState -> {
                this.setBlock(gadget, blockState);
                playerIn.sendStatusMessage(new StringTextComponent(blockState.getBlock().getNameTextComponent().getFormattedText() + " Selected"), true);
            });

            if( !lookingAt.isPresent() ) {
                playerIn.sendStatusMessage(new TranslationTextComponent(LangHelper.key("message.no-block-selected")), true);
            }
        }

        this.collectAndBuild(worldIn, playerIn, gadget);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    private void collectAndBuild(World worldIn, PlayerEntity playerIn, ItemStack gadget) {

    }
}

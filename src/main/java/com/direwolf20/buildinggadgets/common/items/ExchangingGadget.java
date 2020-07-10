package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.modes.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ExchangingGadget extends BuildingGadget {
    private static final List<Mode> MODES = Arrays.asList(
        new SurfaceMode(true),
        new GridMode(true),
        new VerticalColumnMode(true),
        new HorizontalColumnMode(true)
    );

    public ExchangingGadget() {
        super();
    }

    @Override
    public List<Mode> getModes() {
        return MODES;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ActionResult<ItemStack> result = super.onItemRightClick(worldIn, playerIn, handIn);

        return result;
    }

    @Override
    protected void collectAndBuild(World worldIn, @Nullable BlockRayTraceResult trace, PlayerEntity playerIn, ItemStack gadget, BlockState state) {
        if( trace == null ) {
            return;
        }

        // First, get the gadgets mode
        Mode mode = this.getMode(gadget);

        List<BlockPos> blockCollection = mode.getCollection(playerIn, new ModeUseContext(worldIn, state, trace.getPos(), gadget, trace.getFace(), false));
        System.out.println(state);

        blockCollection.forEach(e -> worldIn.setBlockState(e, state));
    }

    @Override
    public void undo(ItemStack gadget, World world, PlayerEntity player) {}
}

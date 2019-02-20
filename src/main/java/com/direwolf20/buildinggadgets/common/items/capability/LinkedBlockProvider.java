package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.building.implementation.SingleTypeProvider;
import com.direwolf20.buildinggadgets.common.building.placement.IBlockProvider;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class LinkedBlockProvider implements IBlockProvider {

    private ItemStack stack;

    public LinkedBlockProvider(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getHandle() {
        return stack;
    }

    @Override
    public IBlockState at(BlockPos pos) {
        return GadgetUtils.getToolBlock(stack);
    }

}

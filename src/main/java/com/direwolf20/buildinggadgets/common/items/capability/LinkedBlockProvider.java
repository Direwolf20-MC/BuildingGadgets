package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.api.building.IBlockProvider;
import com.direwolf20.buildinggadgets.api.building.TranslationWrapper;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

/**
 * Block provider that reads block state from a gadget item. No snapshots will be created therefore it will always be
 * synced to the gadget item.
 */
public final class LinkedBlockProvider implements IBlockProvider {

    private final ItemStack stack;

    public LinkedBlockProvider(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getHandle() {
        return stack;
    }

    @Override
    public IBlockProvider translate(BlockPos origin) {
        return new TranslationWrapper(this, origin);
    }

    @Override
    public BlockState at(BlockPos pos) {
        return GadgetUtils.getToolBlock(stack);
    }

    @Override
    public void serialize(CompoundNBT tag) {
        stack.write(tag);
    }

    @Override
    public IBlockProvider deserialize(CompoundNBT tag) {
        return new LinkedBlockProvider(ItemStack.read(tag));
    }

}

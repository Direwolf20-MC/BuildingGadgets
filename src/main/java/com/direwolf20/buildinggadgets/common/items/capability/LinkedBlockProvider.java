package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.TranslationWrapper;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Block provider that reads block state from a gadget item. No snapshots will be created therefore it will always be
 * synced to the gadget item.
 */
public class LinkedBlockProvider implements IBlockProvider {

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
    public IBlockState at(BlockPos pos) {
        return GadgetUtils.getToolBlock(stack);
    }

    @Override
    public void serialize(NBTTagCompound tag) {
        stack.write(tag);
    }

    @Override
    public IBlockProvider deserialize(NBTTagCompound tag) {
        return new LinkedBlockProvider(ItemStack.read(tag));
    }

}

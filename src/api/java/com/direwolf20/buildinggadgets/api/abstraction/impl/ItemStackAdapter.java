package com.direwolf20.buildinggadgets.api.abstraction.impl;

import com.direwolf20.buildinggadgets.api.abstraction.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ItemStackAdapter implements IItemStack {
    private final ItemStack stack;

    public ItemStackAdapter(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void setCount(int count) {
        stack.setCount(count);
    }

    @Override
    public int getCount() {
        return stack.getCount();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nullable final EnumFacing side) {
        return stack.getCapability(cap, side);
    }
}

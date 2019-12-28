package com.direwolf20.buildinggadgets.common.utils;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public abstract class NetworkIO<P extends NetworkIO.IStackProvider> implements IItemHandler {
    private final List<P> stackProviders;
    protected final EntityPlayer player;

    protected NetworkIO(EntityPlayer player, @Nullable Collection<P> stackProviders) {
        this.player = player;
        this.stackProviders = stackProviders != null ? ImmutableList.copyOf(stackProviders)
                : (ImmutableList<P>) ImmutableList.of(new StackProviderVanilla(ItemStack.EMPTY));
    }

    public static enum Operation {
        EXTRACT, INSERT
    }

    @Override
    public int getSlots() {
        return stackProviders.size();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return getStackProviderInSlot(slot).getStack();
    }

    protected P getStackProviderInSlot(int slot) {
        return stackProviders.get(slot);
    }

    @Nullable
    protected abstract ItemStack insertItemInternal(ItemStack stack, boolean simulate);

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return getNonNullStack(insertItemInternal(stack, simulate));
    }

    @Nonnull
    protected abstract IStackProvider extractItemInternal(P stackProvider, int amount, boolean simulate);

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        P stackProvider = getStackProviderInSlot(slot);
        IStackProvider result = extractItemInternal(stackProvider, amount, simulate);
        stackProvider.shrinkStack(amount);
        return getNonNullStack(result.getStack());
    }

    @Nonnull
    private ItemStack getNonNullStack(@Nullable ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    public static interface IStackProvider {
        @Nonnull
        ItemStack getStack();

        @Nonnull
        void shrinkStack(int amount);
    }

    public static class StackProviderVanilla implements IStackProvider {
        @Nonnull
        private ItemStack stack;

        public StackProviderVanilla(@Nonnull ItemStack stack) {
            this.stack = stack;
        }

        @Override
        @Nonnull
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public void shrinkStack(int amount) {
            stack.grow(-amount);
        }
    }
}
package com.direwolf20.buildinggadgets.common.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.util.Action;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public abstract class NetworkIO implements IItemHandler {
    private final List<ItemStack> stacks;

    protected NetworkIO(Collection<ItemStack> stacks) {
        this.stacks = ImmutableList.copyOf(stacks);
    }

    public static enum Operation {
        EXTRACT, INSERT
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Nullable
    public abstract ItemStack insertItemInternal(ItemStack stack, boolean simulate);

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return getNonNullStack(insertItemInternal(stack, simulate));
    }

    @Nullable
    public abstract ItemStack extractItemInternal(int slot, int amount, boolean simulate);

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return getNonNullStack(extractItemInternal(slot, amount, simulate));
    }

    private ItemStack getNonNullStack(@Nullable ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    public static class NetworkRefinedStorageIO extends NetworkIO {
        private INetwork network;

        public NetworkRefinedStorageIO(INetwork network, Operation operation) {
            super(operation == Operation.EXTRACT ? network.getItemStorageCache().getList().getStacks() : Collections.singletonList(ItemStack.EMPTY));
            this.network = network;
        }

        @Override
        @Nullable
        public ItemStack insertItemInternal(ItemStack stack, boolean simulate) {
            return network.insertItem(stack, stack.getCount(), getAction(simulate));
        }

        @Override
        @Nullable
        public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
            return network.extractItem(getStackInSlot(slot), amount, getAction(simulate));
        }

        private Action getAction(boolean simulate) {
            return simulate ? Action.SIMULATE : Action.PERFORM;
        }
    }
}
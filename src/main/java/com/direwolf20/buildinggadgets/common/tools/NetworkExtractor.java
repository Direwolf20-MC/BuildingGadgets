package com.direwolf20.buildinggadgets.common.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.util.Action;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public abstract class NetworkExtractor implements IItemHandler {
    private final List<ItemStack> stacks;

    protected NetworkExtractor(Collection<ItemStack> stacks) {
        this.stacks = new ArrayList<>(stacks);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = stacks.get(slot);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Nullable
    public abstract ItemStack extractItemInternal(int slot, int amount, boolean simulate);

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = extractItemInternal(slot, amount, simulate);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    public static class NetworkExtractorRS extends NetworkExtractor {
        private INetwork network;

        public NetworkExtractorRS(INetwork network) {
            super(network.getItemStorageCache().getList().getStacks());
            this.network = network;
        }

        @Override
        @Nullable
        public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
            return network.extractItem(getStackInSlot(slot), amount, simulate ? Action.SIMULATE : Action.PERFORM);
        }
    }
}
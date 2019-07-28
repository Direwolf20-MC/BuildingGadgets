package com.direwolf20.buildinggadgets.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public final class InventoryWrapper implements IInventory {
    @Nonnull
    private final IItemHandlerModifiable handler;
    @Nonnull
    private final Predicate<PlayerEntity> usablePredicate;
    @Nonnull
    private final Runnable markDirtyCallback;

    public InventoryWrapper(@Nonnull IItemHandlerModifiable handler) {
        this(handler, null, null);
    }

    public InventoryWrapper(@Nonnull IItemHandlerModifiable handler, @Nullable Predicate<PlayerEntity> usableByPlayerPredicate, @Nullable Runnable markDirtyCallback) {
        this.handler = Objects.requireNonNull(handler, "Cannot construct an InventoryWrapper without an Inventory to wrap.");
        this.usablePredicate = usableByPlayerPredicate != null ? usableByPlayerPredicate : playerEntity -> true;
        this.markDirtyCallback = markDirtyCallback != null ? markDirtyCallback : () -> {};
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return getSizeInventory() <= 0;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        return handler.extractItem(index, count, false);
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        return handler.extractItem(index, getStackInSlot(index).getCount(), false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public void markDirty() {
        markDirtyCallback.run();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return usablePredicate.test(player);
    }

    @Override
    public void clear() {
        for (int i = 0; i < getSizeInventory(); i++) {
            setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }
}

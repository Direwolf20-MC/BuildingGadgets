package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.pastes.GenericPasteContainer;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public final class PasteContainerItemHandler implements IItemHandlerModifiable {
    private final ItemStack container;
    private boolean isCreative;

    public PasteContainerItemHandler(ItemStack container) {
        this.container = container;
        this.isCreative = getContainerItem() instanceof ConstructionPasteContainerCreative;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        setCount(stack.getCount(), false);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        int count = getCount();
        return count <= 0 ? ItemStack.EMPTY : new ItemStack(OurItems.constructionPaste, count);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || ! isItemValid(slot, stack))
            return stack;
        if (isCreative)
            return ItemStack.EMPTY;
        int currentCount = getCount();
        int newCount = setCount(currentCount + stack.getCount(), simulate);
        int dif = newCount - currentCount;
        if (dif == 0)
            return stack;
        stack = stack.copy();
        stack.setCount(stack.getCount() - dif);
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0)
            return ItemStack.EMPTY;

        if (isCreative)
            return new ItemStack(OurItems.constructionPaste, amount);

        int currentCount = getCount();
        int newCount = setCount(currentCount - amount, simulate);

        int dif = currentCount - newCount;
        if (dif == 0)
            return ItemStack.EMPTY;

        return new ItemStack(OurItems.constructionPaste, dif);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getContainerItem().getMaxCapacity();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() == OurItems.constructionPaste;
    }

    private int getCount() {
        return getContainerItem().getPasteCount(container);
    }

    private GenericPasteContainer getContainerItem() {
        return ((GenericPasteContainer) container.getItem());
    }

    private int setCount(int count, boolean simulate) {
        int res = MathHelper.clamp(count, 0, getContainerItem().getMaxCapacity());
        if (! simulate)
            getContainerItem().setPasteCount(container, res);
        return res;
    }
}

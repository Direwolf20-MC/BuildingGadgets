package com.direwolf20.buildinggadgets.api.materials.inventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Optional;

public final class StackHandlerItemHandle implements IObjectHandle<Item> {
    private final IItemHandler handler;
    private final int slot;

    public StackHandlerItemHandle(IItemHandler handler, int slot) {
        this.handler = handler;
        this.slot = slot;
    }

    @Override
    public Class<Item> getIndexClass() {
        return Item.class;
    }

    @Override
    public boolean shouldCleanup() {
        return getStack().isEmpty();
    }

    @Override
    public int match(IUniqueObject<?> item, int count, boolean simulate) {
        ItemStack stack = getStack();
        if (item.matches(stack)) {
            ItemStack resultStack = handler.extractItem(slot, count, simulate);
            return resultStack.getCount();
        }
        return 0;
    }

    @Override
    public Item getIndexObject() {
        return getStack().getItem();
    }

    @Override
    public int insert(IUniqueObject<?> item, int count, boolean simulate) {
        Optional<ItemStack> simpleInsert = item.trySimpleInsert(count);
        if (simpleInsert.isPresent()) {
            ItemStack insertStack = simpleInsert.get();
            ItemStack remaining = handler.insertItem(slot, insertStack, simulate);
            return insertStack.getCount() - remaining.getCount();
        } else if (handler instanceof IItemHandlerModifiable) {
            IItemHandlerModifiable modifiable = (IItemHandlerModifiable) handler;
            ItemStack stack = getStack();
            ItemStack res = item.insertInto(stack, count);
            if (! simulate)
                modifiable.setStackInSlot(slot, res);
            return res.getCount() - stack.getCount();
        }
        return 0;
    }

    private ItemStack getStack() {
        return handler.getStackInSlot(slot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof StackHandlerItemHandle)) return false;

        StackHandlerItemHandle that = (StackHandlerItemHandle) o;

        if (slot != that.slot) return false;
        return handler.equals(that.handler);
    }

    @Override
    public int hashCode() {
        int result = handler.hashCode();
        result = 31 * result + slot;
        return result;
    }
}

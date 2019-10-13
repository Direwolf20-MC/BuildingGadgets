package com.direwolf20.buildinggadgets.common.util.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public final class HandlerInsertProvider implements IInsertProvider {
    private final IItemHandler remoteInventory;

    public HandlerInsertProvider(IItemHandler remoteInventory) {
        this.remoteInventory = remoteInventory;
    }

    @Override
    public int insert(ItemStack stack, int count, boolean simulate) {
        for (int i = 0; i < remoteInventory.getSlots(); i++) {
            if (stack.isEmpty())
                return count;
            stack = remoteInventory.insertItem(count, stack, simulate);
        }
        return count - stack.getCount();
    }
}

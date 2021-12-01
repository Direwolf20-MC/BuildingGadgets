package com.direwolf20.buildinggadgets.common.tainted.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * {@link IInsertProvider} which allows insertion into a single {@link IItemHandler} (for example a remote inventory).
 */
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

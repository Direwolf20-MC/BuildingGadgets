package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.item.ItemStack;

public interface IInsertProvider {
    int insert(ItemStack stack, int count, boolean simulate);
}

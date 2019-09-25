package com.direwolf20.buildinggadgets.common.util.inventory;

import net.minecraft.item.ItemStack;

public interface IInsertExtractProvider {
    ItemStack insert(ItemStack stack, boolean simulate);

    ItemStack extract(ItemStack stack, boolean simulate);
}

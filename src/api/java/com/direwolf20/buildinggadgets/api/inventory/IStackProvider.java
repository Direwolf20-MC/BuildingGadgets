package com.direwolf20.buildinggadgets.api.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IStackProvider {
    NonNullList<ItemStack> getStacksFromStack(ItemStack stack);
}

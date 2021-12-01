package com.direwolf20.buildinggadgets.common.tainted.inventory;

import net.minecraft.world.item.ItemStack;

/**
 * Represents anything that can accept ItemStack insertions. Like for example an {@link net.minecraftforge.items.IItemHandler} or the player's ability to pickup blocks.
 */
public interface IInsertProvider {
    int insert(ItemStack stack, int count, boolean simulate);
}

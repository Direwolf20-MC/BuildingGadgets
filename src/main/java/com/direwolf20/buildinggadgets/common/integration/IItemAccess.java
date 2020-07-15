package com.direwolf20.buildinggadgets.common.integration;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface IItemAccess extends IItemHandler {

	/**
	 * Returns the amount of a certain {@link ItemStack} that this {@link IItemHandler} currently has (and can extract) without modifying it.
	 * @param toCount The ItemStack to count
	 * @param player The player
	 * @return The count of this item in the IItemHandler
	 */
	public int getItemCount(ItemStack toCount, EntityPlayer player);

	/**
	 * Extracts the requested amount (or less) of the specified item from the {@link IItemHandler}.
	 * @param toExtract The {@link ItemStack} to extract
	 * @param maxCount The maximum count that should be extracted
	 * @param player The player
	 * @return The amount that was actually extracted
	 */
	public int extractItems(ItemStack toExtract, int maxCount, EntityPlayer player);
	
}

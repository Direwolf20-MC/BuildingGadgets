package com.direwolf20.buildinggadgets.common.integration;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface IBlockProviderItemHandler extends IItemHandler {

	/**
	 * Returns the amount of a certain ItemStack that this IItemHandler currently has
	 * @param toCount The ItemStack to count
	 * @param player The player
	 * @return The count of this item in the IItemHandler
	 */
	public int getItemCount(ItemStack toCount, EntityPlayer player);

	/**
	 * Extracts the requested amount (or less) of the specified item from the IItemHandler.
	 * @param toExtract The ItemStack to extract
	 * @param maxCount The maximum count that should be extracted
	 * @param player The player
	 * @return The amount that was actually extracted
	 */
	public int extractItems(ItemStack toExtract, int maxCount, EntityPlayer player);
	
}

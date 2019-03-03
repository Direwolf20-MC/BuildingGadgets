package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Such mode should work as if placing atop without any offset.
 */
public interface IAtopSupport {

    BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool, int offset);

}

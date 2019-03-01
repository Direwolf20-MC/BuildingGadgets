package com.direwolf20.buildinggadgets.common.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

public interface IValidatorFactory {

    BiPredicate<BlockPos, IBlockState> createValidatorFor(World world, ItemStack tool, EntityPlayer player, BlockPos initial);

}

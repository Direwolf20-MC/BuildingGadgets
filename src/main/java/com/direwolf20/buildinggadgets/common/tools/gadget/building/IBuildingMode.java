package com.direwolf20.buildinggadgets.common.tools.gadget.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface IBuildingMode {

    List<BlockPos> computeCoordinates(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool);

}

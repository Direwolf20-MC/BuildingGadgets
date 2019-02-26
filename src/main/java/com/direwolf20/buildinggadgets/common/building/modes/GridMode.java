package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.placement.Grid;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class GridMode extends AbstractMode {

    //TODO give config option
    //     min = 1, max = 15 (tool range)
    private static final int DEFAULT_PERIOD_SIZE = 6;
    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "grid");

    public GridMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        EnumFacing locked = sideHit.getAxis().isVertical() ? sideHit : EnumFacing.UP;
        return Grid.create(hit.offset(locked), GadgetUtils.getToolRange(tool), DEFAULT_PERIOD_SIZE);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

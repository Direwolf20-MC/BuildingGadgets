package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.implementation.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.implementation.SingleTypeProvider;
import com.direwolf20.buildinggadgets.common.building.implementation.Wall;
import com.direwolf20.buildinggadgets.common.building.placement.Context;
import com.direwolf20.buildinggadgets.common.building.placement.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
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

public class VerticalWallMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "vertical_wall");

    public VerticalWallMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (sideHit.getAxis().isVertical()) {
            return Wall.extendingSide(hit, sideHit, player.getHorizontalFacing(), range);
        } else {
            return Wall.clickedSide(hit, sideHit, range);
        }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

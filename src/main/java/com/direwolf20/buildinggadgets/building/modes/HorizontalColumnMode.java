package com.direwolf20.buildinggadgets.building.modes;

import com.direwolf20.buildinggadgets.building.placement.Column;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.building.IPlacementSequence;
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

public class HorizontalColumnMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "horizontal_column");

    public HorizontalColumnMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (sideHit.getAxis().isVertical()) {
            return Column.centerAt(hit, player.getHorizontalFacing().rotateY().getAxis(), range);
        }
        //To simulate behavior of build start at the adjacent block of the target position
        return Column.extendFrom(hit, sideHit, range + 1);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.implementation.Stair;
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

public class StairMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "stair");

    public StairMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (hit.getY() > player.posY + 1) {
            return Stair.create(hit.down().offset(sideHit), sideHit, EnumFacing.DOWN, range);
        } else if (hit.getY() < player.posY - 2) {
            return Stair.create(hit.up(), sideHit, EnumFacing.UP, range);
        }
        return Stair.create(hit.up(), sideHit.getOpposite(), EnumFacing.UP, range);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}
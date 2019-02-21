package com.direwolf20.buildinggadgets.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.building.placement.Column;
import com.direwolf20.buildinggadgets.building.IPlacementSequence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * <p>Formal "Build to Me"</p>
 */
public class TargetedAxisChasingMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "axis_chasing");

    public TargetedAxisChasingMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return Column.chaseAxisToEntity(player, hit, sideHit);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

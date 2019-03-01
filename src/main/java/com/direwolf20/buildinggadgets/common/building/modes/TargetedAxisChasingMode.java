package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Column;
import com.direwolf20.buildinggadgets.common.building.placement.ExclusiveAxisChasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * <p>Formal "Build to Me"</p>
 */
public class TargetedAxisChasingMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "axis_chasing");

    public TargetedAxisChasingMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        BlockPos entityPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        //TODO add config "inclusivePlacementForBuildToMe" as a config option
        if (false) {
            return Column.inclusiveAxisChasing(entityPos, hit, sideHit);
        }
        return ExclusiveAxisChasing.create(hit, entityPos, sideHit);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

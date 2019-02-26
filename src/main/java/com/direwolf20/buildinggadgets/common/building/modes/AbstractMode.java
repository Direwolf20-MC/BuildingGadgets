package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.Context;
import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Base class for Building Gadget's native mode implementations.
 */
abstract class AbstractMode implements IBuildingMode {

    protected final Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory;

    AbstractMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    //TODO use capability system
    @Override
    public IBlockProvider getBlockProvider(ItemStack tool) {
        return new SingleTypeProvider(GadgetUtils.getToolBlock(tool));
    }

    @Override
    public BiPredicate<BlockPos, IBlockState> createValidatorFor(World world) {
        return validatorFactory.apply(world);
    }

    @Override
    public Context createExecutionContext(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return new Context(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), validatorFactory);
    }

}

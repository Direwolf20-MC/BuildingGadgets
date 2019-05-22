package com.direwolf20.buildinggadgets.api.building.modes;

import com.direwolf20.buildinggadgets.api.building.Context;
import com.direwolf20.buildinggadgets.api.building.IBuildingMode;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

/**
 * Base class for Building Gadget's native mode implementations to allow reuse validator implementation
 * All ':' in the translation key with '.'.
 */
public abstract class AbstractMode implements IBuildingMode {

    protected final IValidatorFactory validatorFactory;

    public AbstractMode(IValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    public BiPredicate<BlockPos, IBlockState> createValidatorFor(World world, ItemStack tool, EntityPlayer player, BlockPos initial) {
        return validatorFactory.createValidatorFor(world, tool, player, initial);
    }

    @Override
    public Context createExecutionContext(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return new Context(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), validatorFactory);
    }

}

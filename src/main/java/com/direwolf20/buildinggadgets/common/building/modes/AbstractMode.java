package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.Context;
import com.direwolf20.buildinggadgets.common.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

/**
 * Base class for Building Gadget's native mode implementations.
 */
abstract class AbstractMode implements IBuildingMode {

    protected final IValidatorFactory validatorFactory;
    private final String translationKey;

    AbstractMode(IValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
        this.translationKey = "modes." + getRegistryName().toString().replace(':', '.');
    }

    @Override
    public BiPredicate<BlockPos, IBlockState> createValidatorFor(World world, ItemStack tool, EntityPlayer player, BlockPos initial) {
        return validatorFactory.createValidatorFor(world, tool, player, initial);
    }

    @Override
    public Context createExecutionContext(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return new Context(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), validatorFactory);
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

}

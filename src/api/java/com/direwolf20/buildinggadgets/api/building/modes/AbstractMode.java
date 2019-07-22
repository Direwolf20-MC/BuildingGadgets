package com.direwolf20.buildinggadgets.api.building.modes;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.view.IValidatorFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

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
    public BiPredicate<BlockPos, BlockData> createValidatorFor(IWorld world, ItemStack tool, PlayerEntity player, BlockPos initial) {
        return validatorFactory.createValidatorFor(world, tool, player, initial);
    }

}

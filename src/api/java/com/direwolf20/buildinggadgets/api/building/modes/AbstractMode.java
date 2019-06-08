package com.direwolf20.buildinggadgets.api.building.modes;

import com.direwolf20.buildinggadgets.api.building.Context;
import com.direwolf20.buildinggadgets.api.building.IBuildingMode;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
    public BiPredicate<BlockPos, BlockState> createValidatorFor(World world, ItemStack tool, ClientPlayerEntity player, BlockPos initial) {
        return validatorFactory.createValidatorFor(world, tool, player, initial);
    }

    @Override
    public Context createExecutionContext(ClientPlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        return new Context(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), validatorFactory);
    }

}

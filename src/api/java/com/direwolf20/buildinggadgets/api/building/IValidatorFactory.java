package com.direwolf20.buildinggadgets.api.building;

import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

/**
 * Creates a validator for determining whether an build attempt is valid or not. The created validator is bound to a
 * fixed set of parameters such as {@link World},
 */
@FunctionalInterface
public interface IValidatorFactory {

    /**
     * @param world   the world that blocks will be placed in
     * @param tool    the gadget item used to activate the process
     * @param player  the player who activated the process
     * @param initial position selected by the player
     * @return BiPredicate where the first parameter is the attempt position, second parameter is the block that will be
     * placed there.
     */
    BiPredicate<BlockPos, BlockData> createValidatorFor(IWorld world, ItemStack tool, PlayerEntity player, BlockPos initial);

}

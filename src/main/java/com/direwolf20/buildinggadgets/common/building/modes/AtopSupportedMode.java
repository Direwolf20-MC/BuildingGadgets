package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.view.IValidatorFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Base class for modes want to support "Place on Top" modifier.
 */
public abstract class AtopSupportedMode extends AbstractMode {

    public AtopSupportedMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeCoordinates(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        if ((tool.getItem() instanceof IAtopPlacingGadget) && ((IAtopPlacingGadget) tool.getItem()).placeAtop(tool))
            return this.computeWithTransformed(player, transformAtop(player, hit, sideHit, tool), hit, sideHit, tool);
        return this.computeWithTransformed(player, hit, hit, sideHit, tool);
    }

    /**
     * @param player        current player
     * @param transformed   transformed starting position with {@link #transformAtop(PlayerEntity, BlockPos, Direction, ItemStack)}
     * @param original      original starting position
     * @param sideHit       side of block hit
     * @param tool          current gadget
     * @implSpec Implementation should work with {@code transformed.equals(original)}
     *
     * @return {@link IPositionPlacementSequence}
     */
    public abstract IPositionPlacementSequence computeWithTransformed(PlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool);

    /**
     * Calculate the block pos if "Place on Top" was enabled.
     *
     * @param player    target player
     * @param hit       BlockPos hit
     * @param sideHit   side hit
     * @param tool      current gadget
     *
     * @return {@link BlockPos}
     */
    public abstract BlockPos transformAtop(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool);

}

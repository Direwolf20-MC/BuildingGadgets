package com.direwolf20.buildinggadgets.api.building.modes;

import com.direwolf20.buildinggadgets.api.building.IAtopPlacingGadget;
import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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
    public IPositionPlacementSequence computeCoordinates(ClientPlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        if ((tool.getItem() instanceof IAtopPlacingGadget) && ((IAtopPlacingGadget) tool.getItem()).placeAtop(tool))
            return this.computeWithTransformed(player, transformAtop(player, hit, sideHit, tool), hit, sideHit, tool);
        return this.computeWithTransformed(player, hit, hit, sideHit, tool);
    }

    /**
     * @param transformed transformed starting position with {@link #transformAtop(ClientPlayerEntity, BlockPos, Direction, ItemStack)}
     * @param original    original starting position
     * @implSpec Implementation should work with {@code transformed.equals(original)}
     */
    public abstract IPositionPlacementSequence computeWithTransformed(ClientPlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool);

    /**
     * Calculate the block pos if "Place on Top" was enabled.
     */
    public abstract BlockPos transformAtop(ClientPlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool);

}

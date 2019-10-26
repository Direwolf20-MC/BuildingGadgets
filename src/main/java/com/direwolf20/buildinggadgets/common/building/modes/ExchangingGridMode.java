package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.view.IValidatorFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Grid mode designed for Exchanging Gadget where it attempt to build on the same level as target position rather than
 * one higher.
 *
 * @see GridMode
 */
public class ExchangingGridMode extends GridMode {

    public ExchangingGridMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    /**
     * @implNote Exchanger replace at the same level, Building gadget build on top of the level
     */
    @Override
    public IPositionPlacementSequence computeCoordinates(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        return super.computeCoordinates(player, hit, sideHit, tool);
    }

}

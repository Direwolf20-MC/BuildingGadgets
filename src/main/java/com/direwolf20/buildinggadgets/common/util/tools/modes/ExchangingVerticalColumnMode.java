package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.modes.AbstractMode;
import com.direwolf20.buildinggadgets.api.building.placement.Column;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.ModeTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Vertical column mode for Exchanging Gadget.
 * <p>
 * If a 2D x-y coordinate plane was built on the selected side with the selected block as origin, the column will be the
 * Y axis in the plane.
 * The column will be centered at the origin. Length of the column will be the tool range that is floored to an odd
 * number with a lower bound of 1.
 *
 * @see Column
 */
public class ExchangingVerticalColumnMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "vertical_column");

    public ExchangingVerticalColumnMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeCoordinates(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        int radius = MathUtils.floorToOdd(range);
        return Column.centerAt(hit, (sideHit.getAxis().isVertical() ? player.getHorizontalFacing().getAxis() : Axis.Y), radius);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    @Nonnull
    public String getLocalizedName() {
        return ModeTranslation.VERTICAL_COLUMN.format();
    }
}

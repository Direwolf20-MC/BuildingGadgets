package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.Column;
import com.direwolf20.buildinggadgets.api.building.view.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.ModeTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Vertical column mode for Building Gadget.
 * <p>
 * When the player selects top or bottom of a block, it will build a column perpendicular to the ground with length of
 * tool range.
 * <p>
 * When the player selects any horizontal side of a block, it will build a column centered at the selected block with a
 * length of floored tool range.
 *
 * @see Column
 */
public class BuildingVerticalColumnMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "vertical_column");

    public BuildingVerticalColumnMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(PlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (sideHit.getAxis().isVertical())
            return Column.extendFrom(transformed, sideHit, range);
        int radius = MathUtils.floorToOdd(range);
        return Column.centerAt(transformed, Direction.Axis.Y, radius);
    }

    @Override
    public BlockPos transformAtop(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        return hit.offset(sideHit);
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

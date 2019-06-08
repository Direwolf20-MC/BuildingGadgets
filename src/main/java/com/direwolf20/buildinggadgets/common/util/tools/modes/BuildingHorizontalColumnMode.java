package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.Column;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.ModeTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Horizontal column mode for Building Gadget.
 * <p>
 * If the player clicks on any horizontal side of the target position, it will start from the there and build column
 * towards the side clicked. If the player clicks on the top side, however, it will use player's facing instead.
 *
 * @see Column
 */
public class BuildingHorizontalColumnMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "horizontal_column");

    public BuildingHorizontalColumnMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(ClientPlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (sideHit.getAxis().isVertical())
            return Column.extendFrom(transformed, player.getHorizontalFacing(), range);
        return Column.extendFrom(transformed, sideHit.getOpposite(), range);
    }

    @Override
    public BlockPos transformAtop(ClientPlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        return sideHit.getAxis().isVertical() ? hit.offset(player.getHorizontalFacing()) : hit.offset(sideHit.getOpposite());
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    @Nonnull
    public String getLocalizedName() {
        return ModeTranslation.HORIZONTAL_COLUMN.format();
    }

}

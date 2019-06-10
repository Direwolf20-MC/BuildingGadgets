package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.Grid;
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
 * Grid mode for Building Gadget.
 * <p>
 * See {@link Grid} for more information. Period size is can be changed through config, default is {@code 6}
 */
public class GridMode extends AtopSupportedMode {

    //TODO give config option
    //     min = 1, max = 15 (tool range)
    private static final int DEFAULT_PERIOD_SIZE = 6;
    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "grid");

    public GridMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(PlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool) {
        return Grid.create(transformed, GadgetUtils.getToolRange(tool), DEFAULT_PERIOD_SIZE);
    }

    @Override
    public BlockPos transformAtop(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        Direction locked = sideHit.getAxis().isVertical() ? sideHit : Direction.UP;
        return hit.offset(locked);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    @Nonnull
    public String getLocalizedName() {
        return ModeTranslation.GRID.format();
    }
}

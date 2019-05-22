package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.Wall;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.ModeTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Building mode creating a wall which will always be perpendicular to the XZ world plane.
 * <p>
 * When the player selects top or bottom of a block, it will build a plane of blocks where the bottom/top center will
 * locate at the neighboring position from the target.
 * If range is an even number, the wall will be 1 taller than usual. Otherwise the wall will be a square with range as its
 * side length.
 * <p>
 * When the player selects any horizontal side of a block, it will build a plane facing the player centered at
 * the target position.
 * Range used as its side length will be rounded down towards the nearest odd number that is at least 1.
 *
 * @see Wall
 */
public class VerticalWallMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "vertical_wall");

    public VerticalWallMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(EntityPlayer player, BlockPos transformed, BlockPos original, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        int radius = MathUtils.floorToOdd(range) / 2;
        if (sideHit.getAxis().isVertical())
            return Wall.extendingFrom(transformed.offset(sideHit.getOpposite()), sideHit, player.getHorizontalFacing(), radius, MathUtils
                    .isEven(range) ? 1 : 0);
        return Wall.clickedSide(transformed, sideHit, radius);
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return hit.offset(sideHit);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    @Nonnull
    public String getLocalizedName() {
        return ModeTranslation.VERTICAL_WALL.format();
    }
}

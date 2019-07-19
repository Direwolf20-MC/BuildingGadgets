package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.placement.Wall;
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
 * Building mode where such wall will always be perpendicular to the XZ world plane.
 * <p>
 * When the player selects any horizontal side of a block, a wall sitting on clicked side of the target will be build
 * position with a length of tool range.
 * The wall will be a square when tool range is an even number, and 1 higher if tool range is an odd number.
 * <p>
 * When the player selects top or bottom of a block, it will build a wall centered at the target position with tool range.
 * Range used as its side length will be rounded down towards the nearest odd number that is at least 1.
 *
 * @see Wall
 */
public class HorizontalWallMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "horizontal_wall");

    public HorizontalWallMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(PlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        int radius = MathUtils.floorToOdd(range) / 2;
        if (sideHit.getAxis().isVertical())
            return Wall.clickedSide(transformed, sideHit, radius);
        return Wall.extendingFrom(transformed.offset(sideHit.getOpposite()), sideHit, Direction.UP, radius, MathUtils
                .isEven(range) ? 1 : 0);
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
        return ModeTranslation.HORIZONTAL_WALL.format();
    }
}

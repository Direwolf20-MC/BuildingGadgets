package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.Stair;
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
 * Stair mode for Building Gadget.
 * <p>
 * When the target position is higher than player's head, it will extend downwards and towards the player.
 * When the target position is 2 lower than player's feet, it will extend upwards and towards the player.
 * Otherwise, it will extend upwards and away from the player.
 *
 * @see Stair
 */
public class StairMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "stair");

    public StairMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(EntityPlayer player, BlockPos transformed, BlockPos original, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        EnumFacing side = sideHit.getAxis().isVertical() ? player.getHorizontalFacing().getOpposite() : sideHit;

        if (original.getY() > player.posY + 1)
            return Stair.create(transformed, side, EnumFacing.DOWN, range);
        else if (original.getY() < player.posY - 2)
            return Stair.create(transformed, side, EnumFacing.UP, range);
        return Stair.create(transformed, side.getOpposite(), EnumFacing.UP, range);
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        if (hit.getY() > player.posY + 1) {
            EnumFacing side = sideHit.getAxis().isVertical() ? player.getHorizontalFacing() : sideHit;
            return hit.down().offset(side);
        }
        return hit.up();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    @Nonnull
    public String getLocalizedName() {
        return ModeTranslation.STAIR.format();
    }
}

package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.modes.AtopSupportedMode;
import com.direwolf20.buildinggadgets.api.building.placement.ExclusiveAxisChasing;
import com.direwolf20.buildinggadgets.common.util.lang.ModeTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * Logic is backed with {@link ExclusiveAxisChasing} where no attempt will be made at the ending (player) position.
 * <p>
 * This mode is designed for Building Gadget and does not guarantee to work with other gadgets.
 */
public class BuildToMeMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "axis_chasing");

    public BuildToMeMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(PlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool) {
        int range = 25;
        return ExclusiveAxisChasing.create(transformed, new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ)), sideHit, range);
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
        return ModeTranslation.AXIS_CHASING.format();
    }

}

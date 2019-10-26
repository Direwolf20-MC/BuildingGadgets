package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.placement.PlacementSequences.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.building.placement.PlacementSequences.Surface;
import com.direwolf20.buildinggadgets.common.building.view.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
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
 * Surface mode for Building Gadget.
 * <p>
 * It will build on top of every block that has a valid path to the target position if its underside (the block offset
 * towards the side clicked) is same as the underside of the target position.
 * What is a valid path depends on whether the tool uses connected surface mode or not.
 *
 * @see ConnectedSurface
 * @see Surface
 */
public class BuildingSurfaceMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "surface");

    public BuildingSurfaceMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeWithTransformed(PlayerEntity player, BlockPos transformed, BlockPos original, Direction sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        boolean fuzzy = AbstractGadget.getFuzzy(tool);
        if (AbstractGadget.getConnectedArea(tool))
            return ConnectedSurface.create(player.getEntityWorld(), transformed, sideHit.getOpposite(), range, fuzzy);
        return Surface.create(player.getEntityWorld(), transformed, sideHit.getOpposite(), range, fuzzy);
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
        return ModeTranslation.SURFACE.format();
    }

}

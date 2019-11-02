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
 * Surface mode for Exchanging Gadget.
 * <p>
 * Selects blocks that is same as the selected block. Reference region and searching region are the same
 * if compared to {@link BuildingSurfaceMode}.
 *
 * @see BuildingSurfaceMode
 */
public class ExchangingSurfaceMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "surface");

    public ExchangingSurfaceMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPositionPlacementSequence computeCoordinates(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        boolean fuzzy = AbstractGadget.getFuzzy(tool);
        if (AbstractGadget.getConnectedArea(tool))
            return ConnectedSurface.create(player.getEntityWorld(), pos -> pos, hit, sideHit, range, fuzzy);
        return Surface.create(player.getEntityWorld(), pos -> pos, hit, sideHit, range, fuzzy);
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

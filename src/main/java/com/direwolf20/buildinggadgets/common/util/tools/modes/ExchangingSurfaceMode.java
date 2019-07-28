package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.modes.AbstractMode;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.ConnectedSurface;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.Surface;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.Wall;
import com.direwolf20.buildinggadgets.api.building.view.IValidatorFactory;
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
        int range = GadgetUtils.getToolRange(tool) / 2;
        boolean fuzzy = AbstractGadget.getFuzzy(tool);
        Region region = Wall.clickedSide(hit, sideHit, range).getBoundingBox();
        if (AbstractGadget.getConnectedArea(tool))
            return ConnectedSurface.create(player.getEntityWorld(), region, pos -> pos, hit, sideHit, fuzzy);
        return Surface.create(player.getEntityWorld(), hit, region, pos -> pos, fuzzy);
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

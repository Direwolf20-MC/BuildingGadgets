package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.modes.AbstractMode;
import com.direwolf20.buildinggadgets.api.building.placement.ConnectedSurface;
import com.direwolf20.buildinggadgets.api.building.placement.Surface;
import com.direwolf20.buildinggadgets.api.building.placement.Wall;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
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
    public IPositionPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        boolean fuzzy = GadgetGeneric.getFuzzy(tool);
        Region region = Wall.clickedSide(hit, sideHit, range).getBoundingBox();
        if (GadgetGeneric.getConnectedArea(tool))
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

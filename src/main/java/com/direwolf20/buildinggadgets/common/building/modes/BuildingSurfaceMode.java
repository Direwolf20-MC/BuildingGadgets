package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Surface;
import com.direwolf20.buildinggadgets.common.building.placement.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class BuildingSurfaceMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "surface");

    public BuildingSurfaceMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        boolean fuzzy = GadgetGeneric.getFuzzy(tool);
        if (GadgetGeneric.getConnectedArea(tool)) {
            return ConnectedSurface.create(player.world, hit.offset(sideHit), sideHit.getOpposite(), range, fuzzy);
        }
        return Surface.create(player.world, hit.offset(sideHit), sideHit.getOpposite(), range, fuzzy);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

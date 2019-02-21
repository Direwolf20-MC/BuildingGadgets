package com.direwolf20.buildinggadgets.building.modes;

import com.direwolf20.buildinggadgets.building.placement.ConnectedSurface;
import com.direwolf20.buildinggadgets.building.placement.Wall;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class SurfaceMode extends AbstractMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "surface");

    public SurfaceMode(Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (GadgetGeneric.getConnectedArea(tool)) {
            return Wall.clickedSide(hit, sideHit, range);
        }
        return ConnectedSurface.create(hit, sideHit, range);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

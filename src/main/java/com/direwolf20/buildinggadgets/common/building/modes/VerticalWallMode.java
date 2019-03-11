package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Wall;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.IAtopSupport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class VerticalWallMode extends AbstractMode implements IAtopSupport {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "vertical_wall");

    public VerticalWallMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (sideHit.getAxis().isVertical())
            return Wall.extendingFrom(hit, sideHit, player.getHorizontalFacing(), range);
        return Wall.clickedSide(hit.offset(sideHit), sideHit, range);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool, int offset) {
        return hit.offset(sideHit, offset);
    }

}

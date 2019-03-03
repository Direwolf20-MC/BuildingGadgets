package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Stair;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.IAtopSupport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class StairMode extends AbstractMode implements IAtopSupport {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "stair");

    public StairMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        EnumFacing side = sideHit.getAxis().isVertical() ? player.getHorizontalFacing() : sideHit;

        //TODO do it properly instead of a hack fix
        //  and then remove 'original'

        //Will be false if not a GadgetBuilding
        boolean atop = GadgetBuilding.shouldPlaceAtop(tool);
        BlockPos original = atop ? hit : transformAtop(player, hit, sideHit, tool, 1);

        if (original.getY() > player.posY + 1)
            return Stair.create(hit.down().offset(side), side, EnumFacing.DOWN, range);
        else if (original.getY() < player.posY - 2)
            return Stair.create(hit.up(), side, EnumFacing.UP, range);
        return Stair.create(hit.up(), side.getOpposite(), EnumFacing.UP, range);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool, int offset) {
        if (hit.getY() > player.posY + 1) {
            EnumFacing side = sideHit.getAxis().isVertical() ? player.getHorizontalFacing() : sideHit;
            return hit.down(offset).offset(side, offset);
        }
        return hit.up(offset);
    }

}

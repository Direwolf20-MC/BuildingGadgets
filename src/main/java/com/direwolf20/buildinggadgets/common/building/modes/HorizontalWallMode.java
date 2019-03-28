package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Wall;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.MathTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Building mode where such wall will always be perpendicular to the XZ world plane.
 * <p>
 * When the player selects any horizontal side of a block, a wall sitting on clicked side of the target will be build
 * position with a length of tool range.
 * The wall will be a square when tool range is an even number, and 1 higher if tool range is an odd number.
 * <p>
 * When the player selects top or bottom of a block, it will build a wall centered at the target position with tool range.
 * Range used as its side length will be rounded down towards the nearest odd number that is at least 1.
 *
 * @see Wall
 */
public class HorizontalWallMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "horizontal_wall");

    public HorizontalWallMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeWithTransformed(EntityPlayer player, BlockPos transformed, BlockPos original, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        int radius = MathTool.floorToOdd(range) / 2;
        if (sideHit.getAxis().isVertical())
            return Wall.clickedSide(transformed, sideHit, radius);
        return Wall.extendingFrom(transformed.offset(sideHit.getOpposite()), sideHit, EnumFacing.UP, radius, MathTool.isEven(range) ? 1 : 0);
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return hit.offset(sideHit);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

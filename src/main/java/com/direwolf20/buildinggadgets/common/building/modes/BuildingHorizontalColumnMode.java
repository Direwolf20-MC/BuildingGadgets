package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.IValidatorFactory;
import com.direwolf20.buildinggadgets.common.building.placement.Column;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Horizontal column mode for Building Gadget.
 * <p>
 * If the player clicks on any horizontal side of the target position, it will start from the there and build column
 * towards the side clicked. If the player clicks on the top side, however, it will use player's facing instead.
 * </p>
 *
 * @see Column
 */
public class BuildingHorizontalColumnMode extends AtopSupportedMode {

    private static final ResourceLocation NAME = new ResourceLocation(BuildingGadgets.MODID, "horizontal_column");

    public BuildingHorizontalColumnMode(IValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    public IPlacementSequence computeWithTransformed(EntityPlayer player, BlockPos transformed, BlockPos original, EnumFacing sideHit, ItemStack tool) {
        int range = GadgetUtils.getToolRange(tool);
        if (sideHit.getAxis().isVertical())
            return Column.extendFrom(transformed, player.getHorizontalFacing(), range);
        return Column.extendFrom(transformed, sideHit.getOpposite(), range);
    }

    @Override
    public BlockPos transformAtop(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return sideHit.getAxis().isVertical() ? hit.offset(player.getHorizontalFacing()) : hit.offset(sideHit.getOpposite());
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

}

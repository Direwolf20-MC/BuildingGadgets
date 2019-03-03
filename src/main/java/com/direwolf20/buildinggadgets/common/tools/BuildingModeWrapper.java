package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

public class BuildingModeWrapper implements IBuildingMode {

    public static final IAtopSupport NONE = (player, hit, sideHit, tool, offset) -> hit;

    private final IBuildingMode parent;
    private final IAtopSupport support;

    public BuildingModeWrapper(IBuildingMode parent, IAtopSupport support) {
        this.parent = parent;
        this.support = support;
    }

    @Override
    public IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        int offset = GadgetBuilding.shouldPlaceAtop(tool) ? 0 : -1;
        return parent.computeCoordinates(player, support.transformAtop(player, hit, sideHit, tool, offset), sideHit, tool);
    }

    @Override
    public BiPredicate<BlockPos, IBlockState> createValidatorFor(World world, ItemStack tool, EntityPlayer player, BlockPos initial) {
        return parent.createValidatorFor(world, tool, player, initial);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return parent.getRegistryName();
    }

    public boolean doesSupportAtop() {
        return support == NONE;
    }

}

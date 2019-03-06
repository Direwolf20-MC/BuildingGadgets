package com.direwolf20.buildinggadgets.api.template.tilesupport;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface ITileEntityData {
    public ITileDataSerializer getSerializer();

    public default boolean allowPlacementOn(IBlockState state, IWorld world, BlockPos position) {
        return state.isAir(world, position);
    }
}

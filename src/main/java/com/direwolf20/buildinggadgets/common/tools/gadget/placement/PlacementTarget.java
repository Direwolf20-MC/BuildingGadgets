package com.direwolf20.buildinggadgets.common.tools.gadget.placement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacementTarget {

    private final IBlockState state;
    private final BlockPos pos;

    public PlacementTarget(IBlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    public IBlockState getBlockState() {
        return state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void placeIn(World world) {
        world.setBlockState(pos, state);
    }

}

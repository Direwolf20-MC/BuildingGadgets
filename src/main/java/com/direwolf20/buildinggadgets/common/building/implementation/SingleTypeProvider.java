package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.placement.IBlockProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class SingleTypeProvider implements IBlockProvider {

    private final IBlockState state;

    public SingleTypeProvider(IBlockState state) {
        this.state = state;
    }

    @Override
    public IBlockState at(BlockPos pos) {
        return state;
    }

}

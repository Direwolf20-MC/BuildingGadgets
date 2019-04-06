package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeRenderWorld implements IBlockReader {
    private Map<BlockPos, IBlockState> posMap = new HashMap<BlockPos, IBlockState>();
    private World realWorld;


    public void setState(World rWorld, IBlockState setBlock, BlockPos coordinate) {
        this.realWorld = rWorld;
        posMap.put(coordinate, setBlock);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return realWorld.getTileEntity(pos);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return posMap.containsKey(pos) ? posMap.get(pos) : realWorld.getBlockState(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }

}

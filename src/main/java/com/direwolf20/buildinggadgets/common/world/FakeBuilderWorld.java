package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import javax.annotation.Nullable;
import java.util.Set;

public class FakeBuilderWorld implements IBlockReader {
    private Set<BlockPos> positions;
    private IBlockState state;
    private World realWorld;
    private final IBlockState AIR = Blocks.AIR.getDefaultState();

    public void setWorldAndState(World rWorld, IBlockState setBlock, Set<BlockPos> coordinates) {
        this.state = setBlock;
        this.realWorld = rWorld;
        positions = coordinates;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return positions.contains(pos) ? state : AIR;
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }

    public WorldType getWorldType() {
        return realWorld.getWorldType();
    }
}

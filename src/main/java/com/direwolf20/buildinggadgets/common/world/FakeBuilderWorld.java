package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FakeBuilderWorld implements IBlockReader {
    private Set<BlockPos> positions;
    private BlockState state;
    private World realWorld;
    private final BlockState AIR = Blocks.AIR.getDefaultState();

    public void setWorldAndState(World rWorld, BlockState setBlock, Collection<BlockPos> coordinates) {
        this.state = setBlock;
        this.realWorld = rWorld;
        if (coordinates instanceof Set)
            positions = (Set<BlockPos>) coordinates;
        else
            positions = new HashSet<>(coordinates);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
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

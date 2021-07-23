package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MockBuilderWorld implements BlockGetter {
    private Set<BlockPos> positions;
    private BlockState state;
    private Level realWorld;
    private final BlockState AIR = Blocks.AIR.defaultBlockState();

    public void setWorldAndState(Level rWorld, BlockState setBlock, Collection<BlockPos> coordinates) {
        this.state = setBlock;
        this.realWorld = rWorld;
        if (coordinates instanceof Set)
            positions = (Set<BlockPos>) coordinates;
        else
            positions = new HashSet<>(coordinates);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return positions.contains(pos) ? state : AIR;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return null;
    }

}

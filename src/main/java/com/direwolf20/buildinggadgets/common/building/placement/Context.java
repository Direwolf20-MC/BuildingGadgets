package com.direwolf20.buildinggadgets.common.building.placement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

/**
 * Execution context that uses {@link IPlacementSequence}, {@link IBlockProvider} in combination.
 *
 * @implNote Strategy Pattern
 */
public class Context {

    private final IPlacementSequence positions;
    private final IBlockProvider blocks;

    /**
     * It is assumed that this method will return a block provider uses the first value returned by the first value by {@link #positions} as its origin.
     */
    public Context(IPlacementSequence positions, IBlockProvider blocks) {
        this.positions = positions;
        this.blocks = blocks;
    }

    /**
     * Set block states in the world at positions provided by the {@link #getPositionSequence()}.
     *
     * @param world the world that is affected
     * @implNote optimizes object creation if the IPlacementSequence supports {@link NoBorrow} mutable BlockPos output.
     */
    public void place(World world) {
        Iterator<BlockPos> it = positions.iterator();
        if (it instanceof AbstractTargetIterator) {
            this.mutablePlacement(world, (AbstractTargetIterator) it);
        } else {
            this.immutablePlacement(world, it);
        }
    }

    private void mutablePlacement(World world, AbstractTargetIterator it) {
        while (it.hasNext()) {
            MutableBlockPos pos = it.poll();
            world.setBlockState(pos, blocks.at(pos));
        }
    }

    private void immutablePlacement(World world, Iterator<BlockPos> it) {
        while (it.hasNext()) {
            BlockPos pos = it.next();
            world.setBlockState(pos, blocks.at(pos));
        }
    }

    public IPlacementSequence getPositionSequence() {
        return positions;
    }

    public IBlockProvider getBlockProvider() {
        return blocks;
    }

}

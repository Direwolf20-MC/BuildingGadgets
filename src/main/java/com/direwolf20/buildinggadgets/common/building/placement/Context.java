package com.direwolf20.buildinggadgets.common.building.placement;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Execution context that uses {@link IPlacementSequence}, {@link IBlockProvider} in combination.
 *
 * @implNote Strategy Pattern
 */
public class Context {

    private final IPlacementSequence positions;
    private final IBlockProvider blocks;
    private final Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory;

    /**
     * @see Context#Context(IPlacementSequence, IBlockProvider, Function)
     */
    public Context(IPlacementSequence positions, IBlockProvider blocks) {
        this(positions, blocks, world -> (pos, state) -> true);
    }

    /**
     * <p>
     * Note that it is assumed that this method will return a block provider uses the first value returned by the first value by {@link #positions} as its origin.
     * </p>
     */
    public Context(IPlacementSequence positions, IBlockProvider blocks, Function<World, BiPredicate<BlockPos, IBlockState>> validatorFactory) {
        this.positions = positions;
        this.blocks = blocks;
        this.validatorFactory = validatorFactory;
    }

    /**
     * Set block states in the world at positions provided by the {@link #getPositionSequence()}.
     *
     * @param world the world that is affected
     * @implNote optimizes object creation if the IPlacementSequence supports {@link NoBorrow} mutable BlockPos output.
     */
    public void placeIn(World world) {
        Iterator<BlockPos> it = positions.iterator();
        BiPredicate<BlockPos, IBlockState> validator = validatorFactory.apply(world);
        if (it instanceof AbstractTargetIterator) {
            this.mutablePlacement(world, (AbstractTargetIterator) it, validator);
        } else {
            this.immutablePlacement(world, it, validator);
        }
    }

    private void mutablePlacement(World world, AbstractTargetIterator it, BiPredicate<BlockPos, IBlockState> validator) {
        while (it.hasNext()) {
            MutableBlockPos pos = it.poll();
            IBlockState state = blocks.at(pos);
            if (validator.test(pos, state)) {
                world.setBlockState(pos, state);
            }
        }
    }

    private void immutablePlacement(World world, Iterator<BlockPos> it, BiPredicate<BlockPos, IBlockState> validator) {
        while (it.hasNext()) {
            BlockPos pos = it.next();
            IBlockState target = blocks.at(pos);
            if (validator.test(pos, target)) {
                world.setBlockState(pos, target);
            }
        }
    }

    public IPlacementSequence getPositionSequence() {
        return positions;
    }

    /**
     * Wrap raw sequence ({@link #getPositionSequence()}) so that the new iterator only returns positions passes the
     * test of {@link #getValidatorFactory()} with the given World object.
     *
     * @return {@link AbstractIterator} that wraps {@code getPositionSequence().iterator()}
     */
    public Iterator<BlockPos> getFilteredSequence(World world) {
        Iterator<BlockPos> positions = getPositionSequence().iterator();
        BiPredicate<BlockPos, IBlockState> validator = validatorFactory.apply(world);
        return new AbstractIterator<BlockPos>() {
            @Override
            protected BlockPos computeNext() {
                while (positions.hasNext()) {
                    BlockPos next = positions.next();
                    if (validator.test(next, blocks.at(next))) {
                        return next;
                    }
                }
                return endOfData();
            }
        };
    }

    /**
     * @see IPlacementSequence#collect()
     */
    public ImmutableList<BlockPos> collectFilteredSequence(World world) {
        return ImmutableList.copyOf(getFilteredSequence(world));
    }

    public IBlockProvider getBlockProvider() {
        return blocks;
    }

    public Function<World, BiPredicate<BlockPos, IBlockState>> getValidatorFactory() {
        return validatorFactory;
    }

}

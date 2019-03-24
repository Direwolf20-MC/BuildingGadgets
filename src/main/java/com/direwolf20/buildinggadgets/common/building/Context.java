package com.direwolf20.buildinggadgets.common.building;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.function.BiPredicate;

/**
 * Execution context that uses {@link IPlacementSequence} and {@link IBlockProvider} in combination to filter the unusable positions.
 * <p>
 * Testing is done with the predicate produced with {@link #validatorFactory}. If the predicate returns {@code true},
 * the position will be kept and returned in the iterator. If the predicate returns {@code false} on the other hand, the
 * position will be voided.
 *
 * @implNote Execution context in Strategy Pattern
 */
public class Context {

    private final IPlacementSequence positions;
    private final IBlockProvider blocks;
    private final IValidatorFactory validatorFactory;

    /**
     * @see Context#Context(IPlacementSequence, IBlockProvider, IValidatorFactory)
     */
    public Context(IPlacementSequence positions, IBlockProvider blocks) {
        this(positions, blocks, (world, stack, player, initial) -> (pos, state) -> true);
    }

    /**
     * Note that it is assumed that this method will return a block provider uses the first value returned by the first value by {@link #positions} as its translate.
     *
     * @param validatorFactory Creates predicate for determining whether a position should be used or not
     */
    public Context(IPlacementSequence positions, IBlockProvider blocks, IValidatorFactory validatorFactory) {
        this.positions = positions;
        this.blocks = blocks;
        this.validatorFactory = validatorFactory;
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
    public Iterator<BlockPos> getFilteredSequence(World world, ItemStack stack, EntityPlayer player, BlockPos initial) {
        Iterator<BlockPos> positions = getPositionSequence().iterator();
        BiPredicate<BlockPos, IBlockState> validator = validatorFactory.createValidatorFor(world, stack, player, initial);
        return new AbstractIterator<BlockPos>() {
            @Override
            protected BlockPos computeNext() {
                while (positions.hasNext()) {
                    BlockPos next = positions.next();
                    if (validator.test(next, blocks.at(next)))
                        return next;
                }
                return endOfData();
            }
        };
    }

    /**
     * @see IPlacementSequence#collect()
     */
    public ImmutableList<BlockPos> collectFilteredSequence(World world, ItemStack stack, EntityPlayer player, BlockPos initial) {
        return ImmutableList.copyOf(getFilteredSequence(world, stack, player, initial));
    }

    public IBlockProvider getBlockProvider() {
        return blocks;
    }

    public IValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

}

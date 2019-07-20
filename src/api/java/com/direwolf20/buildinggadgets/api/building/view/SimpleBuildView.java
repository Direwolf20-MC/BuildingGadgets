package com.direwolf20.buildinggadgets.api.building.view;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.IBlockProvider;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;

/**
 * Execution context that uses {@link IPositionPlacementSequence} and {@link IBlockProvider} in combination to filter the
 * unusable positions.
 * <p>
 * Testing is done with the predicate produced with {@link #validatorFactory}. If the predicate returns {@code true},
 * the position will be kept and returned in the iterator. If the predicate returns {@code false} on the other hand, the
 * position will be voided.
 *
 * @implNote Execution context in Strategy Pattern
 */
public class SimpleBuildView implements IBuildView {
    private final IPositionPlacementSequence positions;
    private IBlockProvider blocks;
    private final IValidatorFactory validatorFactory;
    private IBuildContext context;
    private BlockPos start;

    /**
     * @see SimpleBuildView#SimpleBuildView(IPositionPlacementSequence, IBlockProvider, IValidatorFactory, IBuildContext, BlockPos)
     */
    public SimpleBuildView(IPositionPlacementSequence positions, IBlockProvider blocks, IBuildContext context) {
        this(positions, blocks, (world, stack, player, initial) -> (pos, state) -> true, context, null);
    }

    /**
     * Note that it is assumed that this method will return a block provider which uses the first value returned by
     * {@link #positions} as translation.
     *
     * @param validatorFactory Creates predicate for determining whether a position should be used or not
     */
    public SimpleBuildView(IPositionPlacementSequence positions, IBlockProvider blocks, IValidatorFactory validatorFactory, IBuildContext buildContext, @Nullable BlockPos start) {
        this.positions = positions;
        this.blocks = blocks;
        this.validatorFactory = validatorFactory;
        Preconditions.checkArgument(buildContext.getBuildingPlayer() != null);
        this.context = buildContext;
        this.start = start;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }

    @Override
    public Iterator<PlacementTarget> iterator() {
        Iterator<BlockPos> posIterator = getFilteredSequence();
        return new AbstractIterator<PlacementTarget>() {
            @Override
            protected PlacementTarget computeNext() {
                if (! posIterator.hasNext())
                    return endOfData();
                BlockPos next = posIterator.next();
                BlockData data = blocks.at(next);
                return new PlacementTarget(next, data);
            }
        };
    }

    @Override
    public IBuildView translateTo(BlockPos pos) {
        blocks = blocks.translate(pos);
        return this;
    }

    @Nullable
    @Override
    public Multiset<UniqueItem> estimateRequiredItems() {
        return null;
    }

    @Override
    public int estimateSize() {
        return - 1;
    }

    @Override
    public void close() {

    }

    @Override
    public IBuildView copy() {
        return new SimpleBuildView(positions, blocks, validatorFactory, context, start);
    }

    @Override
    public Region getBoundingBox() {
        return positions.getBoundingBox();
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return positions.mayContain(x, y, z);
    }

    public IPositionPlacementSequence getPositionSequence() {
        return positions;
    }

    /**
     * Wrap raw sequence ({@link #getPositionSequence()}) so that the new iterator only returns positions passing the
     * test of {@link #getValidatorFactory()} with the given World object.
     *
     * @return {@link AbstractIterator} that wraps {@code getPositionSequence().iterator()}
     */
    public Iterator<BlockPos> getFilteredSequence() {
        Iterator<BlockPos> positions = getPositionSequence().iterator();
        BiPredicate<BlockPos, BlockData> validator = validatorFactory.createValidatorFor(context.getWorld(), context.getUsedStack(), context.getBuildingPlayer(), start);
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
     * @see IPositionPlacementSequence#collect()
     */
    public ImmutableList<BlockPos> collectFilteredSequence() {
        return ImmutableList.copyOf(getFilteredSequence());
    }

    public IBlockProvider getBlockProvider() {
        return blocks;
    }

    public IValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

}

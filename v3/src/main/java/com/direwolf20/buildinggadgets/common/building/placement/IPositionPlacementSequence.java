package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a sequence of blocks where each block is part of the build attempt.
 * <p>
 * The yielding positions should be inside the {@link #getBoundingBox()}. They do not have to be continuous, or yielding
 * in any order, however it should not yield any repeating positions and have a finite number of possible results.
 */
public interface IPositionPlacementSequence extends IPlacementSequence<BlockPos> {

    /**
     * {@inheritDoc}
     * <p>
     * In general it is not necessary to make any considerations whether a given position is suitable for placement, however an implementation may make exceptions from this rule.
     * See {@link PlacementSequences.ConnectedSurface ConnectedSurface} for further information.
     */
    @Override
    Iterator<BlockPos> iterator();

    /**
     * Collect the elements provided by the object to the given {@link Collection}.
     *
     * @param collection    collection
     * @param <T>           collection
     *
     * @return The given {@link Collection} but with all Elements represented by this {@code IPositionPlacementSequence} added to it. Will be the same instance as the parameter.
     */
    default <T extends Collection<? super BlockPos>> T collect(T collection) {
        spliterator().forEachRemaining(collection::add);
        return collection;
    }

    /**
     * Collect the elements into a newly created {@link ImmutableList}
     * @return {@link ImmutableList}
     */
    default ImmutableList<BlockPos> collect() {
        return ImmutableList.copyOf(this);
    }

    /**
     * @return a {@link Stream} representing all positions in this IPositionPlacementSequence
     */
    default Stream<BlockPos> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * @return a copy of the object
     */
    @Override
    IPositionPlacementSequence copy();

}

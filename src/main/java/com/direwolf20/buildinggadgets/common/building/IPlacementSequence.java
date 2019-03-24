package com.direwolf20.buildinggadgets.common.building;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
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
public interface IPlacementSequence extends Iterable<BlockPos> {

    /**
     * {@inheritDoc}
     * <p>
     * In general it is not necessary to make any considerations whether a given position is suitable for placement, however an implementation may make exceptions from this rule.
     * See {@link com.direwolf20.buildinggadgets.common.building.placement.ConnectedSurface ConnectedSurface} for further information.
     */
    @Nonnull
    @Override
    Iterator<BlockPos> iterator();

    /**
     * The bounding box containing all positions which may be produced by this {@code IPlacementSequence}.
     */
    Region getBoundingBox();

    /**
     * <h3>Implementation Contract:</h3>
     * <p>
     * If calculation of whether the given position is a part of the sequence is costly, implementation should do:
     * When the method returns {@code false}, it guaranteed that the position is not a part of the structure. When the
     * the method returns {@code true}, the region may contain the position but it is not guaranteed to be a part of it.
     * <p>
     * In other cases where the process is not costly (ex. does not require loops), implementation should return the exact representation straight up.
     * <p>
     * Please consult the Javadoc of a given implementation for accurate information.
     *
     * @return {@code false} if this PlacementSequence definitely doesn't contain the specified Position. Otherwise {@code true}.
     */
    boolean mayContain(int x, int y, int z);

    /**
     * Collect the elements provided by the object to the given {@link Collection}.
     *
     * @return The given {@link Collection} but with all Elements represented by this {@code IPlacementSequence} added to it. Will be the same instance as the parameter.
     */
    default <T extends Collection<? super BlockPos>> T collect(T collection) {
        iterator().forEachRemaining(collection::add);
        return collection;
    }

    /**
     * Collect the elements into a newly created {@link ImmutableList}
     */
    default ImmutableList<BlockPos> collect() {
        return ImmutableList.copyOf(this);
    }

    /**
     * @return a {@link Stream} representing all positions in this IPlacementSequence
     */
    default Stream<BlockPos> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * @return a copy of the object
     * @implSpec if the object is immutable, this method should be labeled as {@link Deprecated} and state its immutability in its Javadoc.
     * <pre>{@code
     *     /**
     *      * @deprecated Foo should be immutable, so this is not needed
     *      * /
     *     @Deprecated
     *     @Override
     *     public IPlacementSequence copy() {
     *         return new Foo(bar);
     *     }
     * }</pre>
     */
    IPlacementSequence copy();

}

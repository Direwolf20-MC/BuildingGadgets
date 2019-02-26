package com.direwolf20.buildinggadgets.common.building;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents all block that should be changed for the build.
 * <p>
 * The yielding position should be inside the {@link #getBoundingBox()}. They do not have to be continuous, or yielding
 * in any order, however it should not yield any repeating positions and have a finite number of possible results.
 * </p>
 */
public interface IPlacementSequence extends Iterable<BlockPos> {

    /**
     * {@inheritDoc}
     * <p>
     * All returning values should not taking consider of whether the given position is reasonable for replacement,
     * with the exception of terrain-comforting specs (e.g. {@link com.direwolf20.buildinggadgets.common.building.placement.ConnectedSurface ConnectedSurface}).
     * </p>
     */
    @Nonnull
    @Override
    Iterator<BlockPos> iterator();

    /**
     * The bounding box such that all yielding positions are inside the region.
     */
    Region getBoundingBox();

    /**
     * <h3>Implementation Contract:</h3>
     * <p>
     * If calculation of whether the given position is a part of the sequence is costly<sup>1</sup>, implementation should do:
     * When the method returns {@code false}, it guaranteed that the position is not a part of the structure. When the
     * the method returns {@code true}, the region may contain the position but it is not guaranteed to be a part of it.
     * </p><p>
     * In other cases where the process is not costly<sup>1</sup> at all, implementation should return the exact representation straight up.
     * </p>
     *
     * <hr>
     * <p><sup>1: Usually this means the process requires iteration (loops).</sup></p>
     * <p><i>Users should always check the Javadoc provided by the implementation before use</i>, since the contract cannot strictly limit the behavior.</p>
     *
     * @return {@code false} if this PlacementSequence definitely doesn't contain the specified Position. Otherwise {@code true}.
     */
    boolean mayContain(int x, int y, int z);

    /**
     * Collect the elements provided by the object to the given list.
     *
     * @return the parameter
     */
    default <T extends Collection<BlockPos>> T collect(T collection) {
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

package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;

import java.util.List;
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
     * The bounding box such that all yielding positions are inside the region.
     */
    Region getBoundingBox();

    /**
     * @return {@code false} if this PlacementSequence definitely doesn't contain the specified Position. Otherwise {@link true}.
     * @implSpec If the computation is costly, then this {@code IPlacementSequence} may choose to return always true
     */
    boolean contains(int x, int y, int z);

    /**
     * Collect the elements provided by the object to the given list.
     *
     * @return the parameter
     */
    default List<BlockPos> collect(List<BlockPos> list) {
        iterator().forEachRemaining(list::add);
        return list;
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
     *      * / //Trick in include a Javadoc sample in a Javadoc
     *     @Deprecated
     *     @Override
     *     public IPlacementSequence copy() {
     *         return new Foo(bar);
     *     }
     * }</pre>
     */
    IPlacementSequence copy();

}

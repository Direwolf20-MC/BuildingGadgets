package com.direwolf20.buildinggadgets.common.building;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IPlacementSequence<T> extends Iterable<T>{
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), getBoundingBox().size(), 0);
    }

    /**
     * The bounding box containing all positions which may be produced by this {@code IPositionPlacementSequence}.
     * @return {@link Region}
     */
    Region getBoundingBox();

    /**
     * <h3>Implementation Contract:</h3>
     * <p>
     * If calculation of whether the given position is a part of the sequence is costly, implementation should do:
     * When the method returns {@code false}, it guaranteed that the position is not a part of the structure. When the
     * the method returns {@code true}, the region may contain the position but it is not guaranteed to be a part of it.
     * <p>
     * In other cases where the process is not costly, implementation should return the exact representation straight up.
     * <p>
     * Please consult the Javadoc of a given implementation for accurate information.
     *
     * @param x X
     * @param y Y
     * @param z Z
     *
     *  @return {@code false} if this PlacementSequence definitely doesn't contain the specified Position. Otherwise {@code true}.
     */
    boolean mayContain(int x, int y, int z);

    /**
     * @return a copy of the object
     */
    IPlacementSequence<T> copy();
}

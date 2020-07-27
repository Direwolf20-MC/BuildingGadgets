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
     * @return a copy of the object
     */
    IPlacementSequence<T> copy();
}

package com.direwolf20.buildinggadgets.api.util;

import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class DelegatingPlacementSequence<T, U> implements IPlacementSequence<T> {
    private final IPlacementSequence<U> other;
    private final Function<U, T> mapper;

    DelegatingPlacementSequence(IPlacementSequence<U> other, Function<U, T> mapper) {
        this.other = other;
        this.mapper = mapper;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Spliterator<T> spliterator() {
        return new MappingSpliterator<>(other.spliterator(), mapper);
    }

    @Override
    public Iterator<T> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Region getBoundingBox() {
        return other.getBoundingBox();
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return other.mayContain(x, y, z);
    }

    @Override
    public IPlacementSequence<T> copy() {
        return new DelegatingPlacementSequence<>(other.copy(), mapper);
    }
}

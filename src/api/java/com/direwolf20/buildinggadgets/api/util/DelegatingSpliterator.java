package com.direwolf20.buildinggadgets.api.util;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

abstract class DelegatingSpliterator<T, U> implements Spliterator<U> {
    private final Spliterator<T> other;
    private boolean found;

    DelegatingSpliterator(Spliterator<T> other) {
        this.other = Objects.requireNonNull(other);
        found = true;
    }

    @Override
    public boolean tryAdvance(Consumer<? super U> action) {
        found = false;
        while (getOther().tryAdvance(t -> { found = advance(t, action);}) && ! found)
            ;
        return found;
    }

    @Override
    public long estimateSize() {
        return getOther().estimateSize();
    }

    @Override
    public int characteristics() {
        return getOther().characteristics();
    }

    @Override
    public long getExactSizeIfKnown() {
        return getOther().getExactSizeIfKnown();
    }

    abstract boolean advance(T object, Consumer<? super U> action);

    Spliterator<T> getOther() {
        return other;
    }
}

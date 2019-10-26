package com.direwolf20.buildinggadgets.common.util.spliterator;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

//internal do not use, see package-info
public abstract class DelegatingSpliterator<T, U> implements Spliterator<U> {
    private final Spliterator<T> other;
    private boolean found;

    protected DelegatingSpliterator(Spliterator<T> other) {
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
    public int characteristics() {
        return getOther().characteristics() & (~ SORTED);
    }

    @Override
    public long estimateSize() {
        return getOther().estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        return getOther().getExactSizeIfKnown();
    }

    protected abstract boolean advance(T object, Consumer<? super U> action);

    protected Spliterator<T> getOther() {
        return other;
    }
}

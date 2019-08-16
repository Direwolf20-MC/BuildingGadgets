package com.direwolf20.buildinggadgets.api.util;


import javax.annotation.Nullable;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

class FilterSpliterator<T> extends DelegatingSpliterator<T, T> implements Spliterator<T> {
    private final Predicate<T> predicate;

    public FilterSpliterator(Spliterator<T> other, Predicate<T> predicate) {
        super(other);
        this.predicate = predicate;
    }

    @Override
    public boolean advance(T object, Consumer<? super T> action) {
        if (predicate.test(object)) {
            action.accept(object);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Spliterator<T> trySplit() {
        Spliterator<T> split = getOther().trySplit();
        if (split != null)
            return new FilterSpliterator<>(split, predicate);
        return null;
    }
}

package com.direwolf20.buildinggadgets.api.util;


import javax.annotation.Nullable;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MappingSpliterator<T, U> extends DelegatingSpliterator<T, U> {
    private final Function<? super T, ? extends U> mapper;

    public MappingSpliterator(Spliterator<T> other, Function<? super T, ? extends U> mapper) {
        super(other);
        this.mapper = mapper;
    }

    @Override
    public boolean advance(T object, Consumer<? super U> action) {
        action.accept(mapper.apply(object));
        return true;
    }

    @Override
    @Nullable
    public Spliterator<U> trySplit() {
        Spliterator<T> split = getOther().trySplit();
        if (split != null)
            return new MappingSpliterator<>(split, mapper);
        return null;
    }
}

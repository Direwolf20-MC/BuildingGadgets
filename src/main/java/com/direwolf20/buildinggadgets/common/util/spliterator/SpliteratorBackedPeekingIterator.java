package com.direwolf20.buildinggadgets.common.util.spliterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

public final class SpliteratorBackedPeekingIterator<T> extends AbstractIterator<T> implements PeekingIterator<T> {
    private final Iterator<T> adapter;

    public SpliteratorBackedPeekingIterator(Spliterator<T> spliterator) {
        this.adapter = Spliterators.iterator(spliterator);
    }

    @Override
    protected T computeNext() {
        if (adapter.hasNext())
            return adapter.next();

        return endOfData();
    }
}

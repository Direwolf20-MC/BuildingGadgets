package com.direwolf20.buildinggadgets.api.registry;

import com.google.common.collect.ImmutableList;

import java.util.stream.Stream;

public interface IOrderedRegistry<T> extends ISimpleRegistry<T>, Iterable<T> {
    public ImmutableList<T> getValuesInOrder();

    public Stream<T> values();
}

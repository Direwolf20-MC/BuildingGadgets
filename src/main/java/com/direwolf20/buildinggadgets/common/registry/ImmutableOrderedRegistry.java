package com.direwolf20.buildinggadgets.common.registry;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class ImmutableOrderedRegistry<T> implements IOrderedRegistry<T> {
    private final ImmutableBiMap<ResourceLocation, T> backingMap;
    private final ImmutableList<T> orderedValues;

    ImmutableOrderedRegistry(Map<ResourceLocation, T> backingMap, List<T> orderedValues) {
        this.backingMap = ImmutableBiMap.copyOf(backingMap);
        this.orderedValues = ImmutableList.copyOf(orderedValues);
    }

    @Override
    @Nullable
    public T get(ResourceLocation key) {
        return backingMap.get(key);
    }

    @Override
    public boolean contains(ResourceLocation key) {
        return backingMap.containsKey(key);
    }

    @Override
    public ImmutableList<T> getValuesInOrder() {
        return orderedValues;
    }

    @Override
    public Stream<T> values() {
        return orderedValues.stream();
    }

    @Override
    public Iterator<T> iterator() {
        return orderedValues.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        orderedValues.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return orderedValues.spliterator();
    }

    @Nullable
    @Override
    public ResourceLocation getKey(T value) {
        return backingMap.inverse().get(value);
    }
}

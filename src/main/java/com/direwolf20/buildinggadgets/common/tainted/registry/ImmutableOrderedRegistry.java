package com.direwolf20.buildinggadgets.common.tainted.registry;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ImmutableOrderedRegistry<T> {
    private final ImmutableBiMap<ResourceLocation, T> backingMap;
    private final ImmutableList<T> orderedValues;

    ImmutableOrderedRegistry(Map<ResourceLocation, T> backingMap, List<T> orderedValues) {
        this.backingMap = ImmutableBiMap.copyOf(backingMap);
        this.orderedValues = ImmutableList.copyOf(orderedValues);
    }

    @Nullable
    public T get(ResourceLocation key) {
        return backingMap.get(key);
    }

    public boolean contains(ResourceLocation key) {
        return backingMap.containsKey(key);
    }

    public ImmutableList<T> getValuesInOrder() {
        return orderedValues;
    }

    public Stream<T> values() {
        return orderedValues.stream();
    }

    public Iterator<T> iterator() {
        return orderedValues.iterator();
    }

    public void forEach(Consumer<? super T> action) {
        orderedValues.forEach(action);
    }

    public Spliterator<T> spliterator() {
        return orderedValues.spliterator();
    }

    @Nullable
    public ResourceLocation getKey(T value) {
        return backingMap.inverse().get(value);
    }
}

package com.direwolf20.buildinggadgets.api.registry;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class TopologicalRegistryBuilder<T> {
    private final MutableGraph<ValueObject<T>> theGraph;
    private final SortedMap<ResourceLocation, ValueObject<T>> values;
    private boolean build;

    public static <T> TopologicalRegistryBuilder<T> create() {
        return new TopologicalRegistryBuilder<>();
    }

    private TopologicalRegistryBuilder() {
        this.theGraph = GraphBuilder.directed().allowsSelfLoops(false).build();
        this.values = new TreeMap<>();
        this.build = false;
    }

    public TopologicalRegistryBuilder<T> addAllValues(Map<ResourceLocation, T> values) {
        for (Map.Entry<ResourceLocation, T> entry : values.entrySet()) {
            addValue(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public TopologicalRegistryBuilder<T> addValue(ResourceLocation key, T value) {
        validateUnbuild();
        validateNotRegisteredTwice(Objects.requireNonNull(key));
        ValueObject<T> obj = new ValueObject<>(key, Objects.requireNonNull(value));
        values.put(key, obj);
        theGraph.addNode(obj);
        return this;
    }

    public TopologicalRegistryBuilder<T> addAllMarkers(Iterable<ResourceLocation> markers) {
        for (ResourceLocation marker : markers) {
            addMarker(marker);
        }
        return this;
    }

    public TopologicalRegistryBuilder<T> addMarker(ResourceLocation marker) {
        validateUnbuild();
        validateNotRegisteredTwice(Objects.requireNonNull(marker));
        ValueObject<T> obj = new ValueObject<>(marker, null);
        values.put(marker, obj);
        theGraph.addNode(obj);
        return this;
    }

    public TopologicalRegistryBuilder<T> addDependency(ResourceLocation source, ResourceLocation dependent) {
        validateUnbuild();
        ValueObject<T> sourceObj = values.get(Objects.requireNonNull(source));
        ValueObject<T> dependentObj = values.get(Objects.requireNonNull(dependent));
        Preconditions.checkArgument(sourceObj != null, "Cannot add dependency on unknown source key %s", source.toString());
        Preconditions.checkArgument(dependentObj != null, "Cannot add dependency for unknown dependent key %s", dependent.toString());
        theGraph.putEdge(sourceObj, dependentObj);
        return this;
    }

    public IOrderedRegistry<T> build() {
        validateUnbuild();
        build = true;
        values.clear();
        List<ValueObject<T>> sorted = TopologicalSort.topologicalSort(theGraph, Comparator.naturalOrder());
        final List<T> objs = new ArrayList<>(sorted.size());
        final Map<ResourceLocation, T> map = new HashMap<>();
        sorted.stream()
                .filter(obj -> obj.getValue() != null)
                .forEach(obj -> {
                    objs.add(obj.getValue());
                    map.put(obj.getKey(), obj.getValue());
                });
        return new ImmutableOrderedRegistry<>(map, objs);
    }

    private void validateNotRegisteredTwice(ResourceLocation key) {
        Preconditions.checkArgument(! values.containsKey(key), "Cannot register %s twice!", key.toString());
    }

    private void validateUnbuild() {
        Preconditions.checkState(! build, "Cannot access already created Builder!");
    }

    private static final class ValueObject<T> implements Comparable<ValueObject<?>> {
        @Nonnull
        private final ResourceLocation key;
        @Nullable
        private final T value;

        private ValueObject(@Nonnull ResourceLocation key, @Nullable T value) {
            this.key = key;
            this.value = value;
        }

        @Nonnull
        private ResourceLocation getKey() {
            return key;
        }

        @Nullable
        private T getValue() {
            return value;
        }

        @Override
        public int compareTo(ValueObject<?> o) {
            return getKey().compareTo(o.getKey());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (! (o instanceof ValueObject)) return false;

            ValueObject<?> that = (ValueObject<?>) o;

            return getKey().equals(that.getKey());
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .omitNullValues()
                    .add("key", key)
                    .add("value", value)
                    .toString();
        }
    }
}

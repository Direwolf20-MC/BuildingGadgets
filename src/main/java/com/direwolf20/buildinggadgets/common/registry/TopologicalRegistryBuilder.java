package com.direwolf20.buildinggadgets.common.registry;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class TopologicalRegistryBuilder<T> {
    private final MutableGraph<ValueObject<T>> theGraph;
    private final Map<ResourceLocation, ValueObject<T>> values;
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
        ValueObject<T> obj;
        Preconditions.checkArgument(! containsValue(value), "Cannot have duplicate values, as the mapping needs to be bijective!");
        if (values.containsKey(Objects.requireNonNull(key))) {
            obj = values.get(key);
            obj.setValue(value);//override existing value
        } else {
            obj = new ValueObject<>(key, value);
            values.put(key, obj);
            theGraph.addNode(obj);
        }
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
        if (values.containsKey(Objects.requireNonNull(marker)))
            return this;
        ValueObject<T> obj = new ValueObject<>(marker, null);
        values.put(marker, obj);
        theGraph.addNode(obj);
        return this;
    }

    public TopologicalRegistryBuilder<T> addDependency(ResourceLocation source, ResourceLocation dependent) {
        validateUnbuild();
        if (! values.containsKey(source))
            addMarker(source);
        if (! values.containsKey(dependent))
            addMarker(dependent);
        ValueObject<T> sourceObj = values.get(Objects.requireNonNull(source));
        ValueObject<T> dependentObj = values.get(Objects.requireNonNull(dependent));
        theGraph.putEdge(sourceObj, dependentObj);
        return this;
    }

    public TopologicalRegistryBuilder<T> merge(TopologicalRegistryBuilder<T> other) {
        validateUnbuild();
        for (ValueObject<T> node : other.theGraph.nodes()) {
            if (node.getValue() != null)
                addValue(node.getKey(), node.getValue());//perform a copy... values are mutable...
            else
                addMarker(node.getKey());
        }
        for (EndpointPair<ValueObject<T>> edge : other.theGraph.edges()) {
            addDependency(edge.source().getKey(), edge.target().getKey());
        }
        return this;
    }

    public IOrderedRegistry<T> build() {
        validateUnbuild();
        build = true;
        values.clear();
        List<ValueObject<T>> sorted = TopologicalSort.topologicalSort(theGraph, Comparator.naturalOrder());
        final ImmutableList.Builder<T> objs = ImmutableList.builder();
        final ImmutableBiMap.Builder<ResourceLocation, T> map = ImmutableBiMap.builder();
        sorted.stream().filter(val -> val.getValue() != null).forEach(obj -> {
            objs.add(obj.getValue());
            map.put(obj.getKey(), obj.getValue());
        });
        return new ImmutableOrderedRegistry<>(map.build(), objs.build());
    }

    private void validateUnbuild() {
        Preconditions.checkState(! build, "Cannot access already created Builder!");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("values", values.values())
                .add("graph", "MutableGraph{" + theGraph + "}")
                .toString();
    }

    private boolean containsValue(T value) {
        //needs iterating as we cannot use a BiMap here, because we cannot do a value check on ValueObject, as this would contradict equals transitivity
        for (ValueObject<T> existing : values.values()) {
            if (existing.getValue() == value || (existing.getValue() != null && existing.getValue().equals(value)))
                return true;
        }
        return false;
    }

    private static final class ValueObject<T> implements Comparable<ValueObject<?>> {
        @Nonnull
        private final ResourceLocation key;
        @Nullable
        private T value;

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

        public void setValue(@Nonnull T value) {
            this.value = Objects.requireNonNull(value);
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

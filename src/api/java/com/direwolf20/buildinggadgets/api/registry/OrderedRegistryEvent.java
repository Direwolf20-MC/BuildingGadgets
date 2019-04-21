package com.direwolf20.buildinggadgets.api.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.GenericEvent;

import java.util.Map;

public class OrderedRegistryEvent<T> extends GenericEvent<T> {
    private final TopologicalRegistryBuilder<T> builder;

    public OrderedRegistryEvent(Class<T> type, TopologicalRegistryBuilder<T> builder) {
        super(type);
        this.builder = builder;
    }

    public TopologicalRegistryBuilder<T> addAllValues(Map<ResourceLocation, T> values) {
        return builder.addAllValues(values);
    }

    public TopologicalRegistryBuilder<T> addValue(ResourceLocation key, T value) {
        return builder.addValue(key, value);
    }

    public TopologicalRegistryBuilder<T> addAllMarkers(Iterable<ResourceLocation> markers) {
        return builder.addAllMarkers(markers);
    }

    public TopologicalRegistryBuilder<T> addMarker(ResourceLocation marker) {
        return builder.addMarker(marker);
    }

    public TopologicalRegistryBuilder<T> addDependency(ResourceLocation source, ResourceLocation dependent) {
        return builder.addDependency(source, dependent);
    }
}

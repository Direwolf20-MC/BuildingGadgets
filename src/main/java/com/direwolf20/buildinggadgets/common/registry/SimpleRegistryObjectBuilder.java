package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.registry.SimpleRegistryObjectBuilder.ConstantObjectBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Objects;
import java.util.function.Function;

public class SimpleRegistryObjectBuilder<T extends IForgeRegistryEntry<T>> extends RegistryObjectBuilder<T, ConstantObjectBuilder<T>> {
    private T obj;
    public SimpleRegistryObjectBuilder(String registryName) {
        super(registryName);
    }

    public SimpleRegistryObjectBuilder(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public SimpleRegistryObjectBuilder<T> factory(Function<ConstantObjectBuilder<T>, T> factory) {
        throw new AssertionError("This Object does not provide a builder. It should therefore also not need a factory.");
    }

    @Override
    public SimpleRegistryObjectBuilder<T> builder(ConstantObjectBuilder<T> builder) {
        throw new AssertionError("This Object does not provide a builder. It should therefore also not need a builder.");
    }

    public SimpleRegistryObjectBuilder<T> object(T obj) {
        super.builder(new ConstantObjectBuilder<>(obj));
        super.factory(ConstantObjectBuilder::build);
        this.obj = obj;
        return this;
    }

    public T getObj() {
        return obj;
    }

    static class ConstantObjectBuilder<T extends IForgeRegistryEntry<T>> {
        private final T object;

        ConstantObjectBuilder(T object) {
            this.object = Objects.requireNonNull(object);
        }

        public T build() {
            return object;
        }
    }
}

package com.direwolf20.buildinggadgets.common.registry;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface ISimpleRegistry<T> {
    @Nullable
    T get(ResourceLocation key);

    @Nullable
    ResourceLocation getKey(T value);

    boolean contains(ResourceLocation key);
}

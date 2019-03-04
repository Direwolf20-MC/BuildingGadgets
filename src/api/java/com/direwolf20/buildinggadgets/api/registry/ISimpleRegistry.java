package com.direwolf20.buildinggadgets.api.registry;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface ISimpleRegistry<T> {
    @Nullable
    public T get(ResourceLocation key);

    public boolean contains(ResourceLocation key);
}

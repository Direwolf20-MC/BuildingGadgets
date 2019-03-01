package com.direwolf20.buildinggadgets.api.registry;

import net.minecraft.util.ResourceLocation;

public interface ISimpleRegistry<T> {
    public T get(ResourceLocation key);

    public boolean contains(ResourceLocation key);
}

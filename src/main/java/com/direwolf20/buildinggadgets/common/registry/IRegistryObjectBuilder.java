package com.direwolf20.buildinggadgets.common.registry;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IRegistryObjectBuilder <T extends IForgeRegistryEntry<T>> {
    public T construct();
}

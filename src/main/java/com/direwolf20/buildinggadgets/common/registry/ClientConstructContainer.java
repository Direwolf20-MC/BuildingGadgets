package com.direwolf20.buildinggadgets.common.registry;

import net.minecraftforge.registries.IForgeRegistryEntry;

public class ClientConstructContainer<T extends IForgeRegistryEntry<T>, B extends RegistryObjectBuilder<T, ?>> extends RegistryContainer<T, B> {
    public void clientInit() {

    }
}

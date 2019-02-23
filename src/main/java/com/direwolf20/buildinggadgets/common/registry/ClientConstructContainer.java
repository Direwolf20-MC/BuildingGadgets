package com.direwolf20.buildinggadgets.common.registry;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ClientConstructContainer<T extends IForgeRegistryEntry<T>, B extends IRegistryObjectBuilder<T>> extends RegistryContainer<T,B> {
    public void clientInit() {

    }
}

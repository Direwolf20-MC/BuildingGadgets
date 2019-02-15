package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.base.Preconditions;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RegistryContainer <T extends IForgeRegistryEntry<T>, B extends IRegistryObjectBuilder<T>> {
    private Set<B> builders;
    public RegistryContainer() {
        builders = new HashSet<>();
        FMLModLoadingContext.get().getModEventBus().register(this);
    }

    public void add(B builder){
        Preconditions.checkArgument(!builders.contains(Objects.requireNonNull(builder)),"Cannot register builder twice!");
        builders.add(builder);
    }

    protected Set<B> getBuilders() {
        return builders;
    }

    @SubscribeEvent
    public void register(RegistryEvent.Register<T> event) {
        BuildingGadgets.LOG.debug("Registering BuildingGadgets objects to {}.",event.getName());
        for (B builder:getBuilders()) {
            event.getRegistry().register(builder.construct());
        }
        BuildingGadgets.LOG.debug("Finished Registering {}BuildingGadgets objects to {}.",getBuilders().size(),event.getName());
    }
}

package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RegistryContainer<T extends IForgeRegistryEntry<T>, B extends RegistryObjectBuilder<T, ?>> {
    private Set<B> builders;
    public RegistryContainer() {
        builders = new HashSet<>();
    }

    public void add(B builder){
        Preconditions.checkArgument(!builders.contains(Objects.requireNonNull(builder)),"Cannot register builder twice!");
        builders.add(builder);
    }

    protected Set<B> getBuilders() {
        return builders;
    }

    public void register(RegistryEvent.Register<T> event) {
        BuildingGadgets.LOG.debug("Registering {} objects to {}.", Reference.MODID, event.getName());
        for (B builder:getBuilders()) {
            event.getRegistry().register(builder.construct());
        }
        BuildingGadgets.LOG
                .debug("Finished Registering {} {} objects to {}.", getBuilders().size(), Reference.MODID, event
                        .getName());
    }

    public void clear() {
        builders.clear();
    }
}

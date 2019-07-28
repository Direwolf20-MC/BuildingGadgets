package com.direwolf20.buildinggadgets.common.registry.container;

import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class ScreenContainerObjectBuilder extends RegistryObjectBuilder<ContainerType<?>, ScreenContainerBuilder<?, ?>> {
    public ScreenContainerObjectBuilder(String registryName) {
        super(registryName);
    }

    public ScreenContainerObjectBuilder(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public ScreenContainerObjectBuilder builder(ScreenContainerBuilder<?, ?> builder) {
        super.builder(builder);
        super.factory(ScreenContainerBuilder::getOrCreate);
        return this;
    }

    @Override
    public ScreenContainerObjectBuilder factory(Function<ScreenContainerBuilder<?, ?>, ContainerType<?>> factory) {
        throw new AssertionError();
    }


    void registerScreen() {
        getBuilder().registerScreen();
    }


}

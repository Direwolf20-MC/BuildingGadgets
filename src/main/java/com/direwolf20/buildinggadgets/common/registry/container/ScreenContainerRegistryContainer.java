package com.direwolf20.buildinggadgets.common.registry.container;

import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import net.minecraft.inventory.container.ContainerType;

public class ScreenContainerRegistryContainer extends RegistryContainer<ContainerType<?>, ScreenContainerObjectBuilder> {

    public ScreenContainerRegistryContainer() {
        super();
    }

    public void clientSetup() {
        for (ScreenContainerObjectBuilder builder : getBuilders()) {
            builder.registerScreen();
        }
    }


}

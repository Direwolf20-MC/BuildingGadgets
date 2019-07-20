package com.direwolf20.buildinggadgets.common.registry.container;

import com.direwolf20.buildinggadgets.common.registry.ClientConstructContainer;
import net.minecraft.inventory.container.ContainerType;

public class ScreenContainerRegistryContainer extends ClientConstructContainer<ContainerType<?>, ScreenContainerObjectBuilder> {
    @Override
    public void clientInit() {
        super.clientInit();
        for (ScreenContainerObjectBuilder builder : getBuilders()) {
            builder.registerScreen();
        }
    }


}

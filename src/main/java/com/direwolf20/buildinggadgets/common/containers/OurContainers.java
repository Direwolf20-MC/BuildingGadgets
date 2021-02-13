package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class OurContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, BuildingGadgetsAPI.MODID);

    public static final RegistryObject<ContainerType<TemplateManagerContainer>> TEMPLATE_MANAGER_CONTAINER
            = CONTAINERS.register("template_manager_container", () -> IForgeContainerType.create(TemplateManagerContainer::new));
}

package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class OurContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Reference.MODID);

    public static final RegistryObject<MenuType<TemplateManagerContainer>> TEMPLATE_MANAGER_CONTAINER
            = CONTAINERS.register("template_manager_container", () -> IForgeContainerType.create(TemplateManagerContainer::new));
}

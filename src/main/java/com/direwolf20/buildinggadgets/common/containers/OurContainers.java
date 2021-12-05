package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OurContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Reference.MODID);

    public static final RegistryObject<MenuType<TemplateManagerContainer>> TEMPLATE_MANAGER_CONTAINER
            = CONTAINERS.register("template_manager_container", () -> IForgeMenuType.create(TemplateManagerContainer::new));
}

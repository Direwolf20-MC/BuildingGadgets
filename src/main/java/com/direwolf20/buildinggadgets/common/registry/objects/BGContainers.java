package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.SimpleRegistryObjectBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ContainerReference;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MODID)
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class BGContainers {
    private static final RegistryContainer<ContainerType<?>, SimpleRegistryObjectBuilder<ContainerType<?>>> container = new RegistryContainer<>();

    private BGContainers() {}

    @ObjectHolder(ContainerReference.TEMPLATE_MANAGER_CONTAINER)
    public static ContainerType<TemplateManagerContainer> TEMPLATE_MANAGER_CONTAINER;

    static void init() {
        container.add(
                new SimpleRegistryObjectBuilder<ContainerType<?>>(ContainerReference.TEMPLATE_MANAGER_CONTAINER_RL)
                        .object(new ContainerType<>((IContainerFactory<TemplateManagerContainer>)
                                ((wId, inv, buffer) -> new TemplateManagerContainer(wId, inv))))
        );
    }

    static void cleanup() {
        container.clear();
    }

    @SubscribeEvent
    public static void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        //container.register(event);
        event.getRegistry().registerAll(
                IForgeContainerType.create(TemplateManagerContainer::new).setRegistryName("template_manager_container")
        );
    }
}

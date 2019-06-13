package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ContainerReference;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Objects;

@ObjectHolder(Reference.MODID)
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class BGContainers {
    private static final RegistryContainer<ContainerType<?>, RegistryObjectBuilder<ContainerType<?>, ContainerTypeBuilder<?>>> container = new RegistryContainer<>();

    private BGContainers() {}

    @ObjectHolder(ContainerReference.TEMPLATE_MANAGER_CONTAINER)
    public static ContainerType<?> TEMPLATE_MANAGER_CONTAINER;

    static void init() {
        container.add(
                new RegistryObjectBuilder<ContainerType<?>, ContainerTypeBuilder<?>>(ContainerReference.TEMPLATE_MANAGER_CONTAINER_RL)
                        .builder(new ContainerTypeBuilder<>((wId, inv, buffer) -> new TemplateManagerContainer(wId, inv))));
    }

    static void cleanup() {
        container.clear();
    }

    @SubscribeEvent
    public static void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        container.register(event);
    }

    private static class ContainerTypeBuilder<T extends Container> {
        private final ContainerType.IFactory<T> factory;

        public ContainerTypeBuilder(IContainerFactory<T> factory) {
            this.factory = Objects.requireNonNull(factory);
        }

        public ContainerType<T> build() {
            return new ContainerType<>(factory);
        }
    }
}

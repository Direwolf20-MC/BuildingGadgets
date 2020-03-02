package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.client.gui.blocks.ChargingStationGUI;
//import com.direwolf20.buildinggadgets.client.gui.blocks.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.containers.ChargingStationContainer;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ContainerReference;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class OurContainers {
    private OurContainers() {} 
    @ObjectHolder(ContainerReference.TEMPLATE_MANAGER_CONTAINER)
    public final static ContainerType<TemplateManagerContainer> TEMPLATE_MANAGER_CONTAINER = IForgeContainerType.create(TemplateManagerContainer::new);

    @ObjectHolder(ContainerReference.CHARGING_STATION_CONTAINER)
    public final static ContainerType<ChargingStationContainer> CHARGING_STATION_CONTAINER = IForgeContainerType.create(ChargingStationContainer::new);

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        IForgeRegistry<ContainerType<?>> registry = event.getRegistry();

//        registry.register(TEMPLATE_MANAGER_CONTAINER.setRegistryName(ContainerReference.TEMPLATE_MANAGER_CONTAINER_RL));
        registry.register(CHARGING_STATION_CONTAINER.setRegistryName(ContainerReference.CHARGING_STATION_CONTAINER_RL));
    }

    /**
     * Called from some Client Dist runner in the main class
     */
    public static void registerContainerScreens() {
//        ScreenManager.<TemplateManagerContainer, TemplateManagerGUI>registerFactory(TEMPLATE_MANAGER_CONTAINER, TemplateManagerGUI::new);
        ScreenManager.<ChargingStationContainer, ChargingStationGUI>registerFactory(CHARGING_STATION_CONTAINER, ChargingStationGUI::new);
    }
}

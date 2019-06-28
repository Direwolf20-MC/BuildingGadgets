package com.direwolf20.buildinggadgets.api;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = APIReference.MODID)
public final class BuildinggadgetsAPI {
    public static final Logger LOG = LogManager.getLogger();

    public BuildinggadgetsAPI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerRegistries);
        modEventBus.addListener(this::setup);
    }

    private void registerRegistries(RegistryEvent.NewRegistry event) {
        Registries.onCreateRegistries();
    }

    private void setup(FMLCommonSetupEvent event) {
        event.getIMCStream().forEach(this::handleIMC);
        Registries.createOrderedRegistries();
    }

    private void handleIMC(InterModComms.IMCMessage message) {
        Registries.handleIMC(message);
    }
}
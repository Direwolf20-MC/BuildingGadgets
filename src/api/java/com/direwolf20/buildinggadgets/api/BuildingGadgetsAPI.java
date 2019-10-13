package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.api.capability.CapabilityBlockProvider;
import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BuildingGadgetsAPI {
    public static final Logger LOG = LogManager.getLogger();

    public BuildingGadgetsAPI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerRegistries);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::handleIMC);
    }

    private void registerRegistries(RegistryEvent.NewRegistry event) {
        Registries.onCreateRegistries();
    }

    private void setup(FMLCommonSetupEvent event) {
        CapabilityBlockProvider.register();
        CapabilityTemplate.register();
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        Registries.createOrderedRegistries();
    }

    private void handleIMC(InterModProcessEvent event) {
        event.getIMCStream().forEach(this::handleIMCMessage);
    }

    private void handleIMCMessage(InterModComms.IMCMessage message) {
        if (Registries.handleIMC(message))
            LOG.trace("Successfully handled IMC-Message using Method {} from Mod {}.", message.getMethod(), message.getSenderModId());
        else
            LOG.warn("Failed to handle IMC-Message using Method {} from Mod {}!", message.getMethod(), message.getSenderModId());
    }
}

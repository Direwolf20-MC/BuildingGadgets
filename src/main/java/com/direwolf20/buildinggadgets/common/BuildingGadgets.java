package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.capability.CapabilityBlockProvider;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.config.crafting.RecipeConstructionPaste.Serializer;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.registry.Registries;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.save.TemplateSave;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.command.Commands;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

@Mod(value = Reference.MODID)
public final class BuildingGadgets {

    public static Logger LOG = LogManager.getLogger();
    private static BuildingGadgets theMod = null;

    public static BuildingGadgets getInstance() {
        assert theMod != null;
        return theMod;
    }

    private TemplateSave copyPasteSave;

    public BuildingGadgets() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(Type.COMMON, Config.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_CONFIG);

        MinecraftForge.EVENT_BUS.addListener(this::serverLoad);
        MinecraftForge.EVENT_BUS.addListener(this::serverLoaded);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        eventBus.addListener(this::registerRegistries);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::loadComplete);
        eventBus.addListener(this::handleIMC);
        eventBus.addGenericListener(IRecipeSerializer.class, this::onRecipeRegister);

        eventBus.addListener(Config::onLoad);
        eventBus.addListener(Config::onFileChange);
        eventBus.addListener(this::onEnqueueIMC);

        // Client only registering
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            eventBus.addListener((Consumer<FMLClientSetupEvent>) event -> ClientProxy.clientSetup(eventBus));
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen);
        });

        Registries.setup();
    }

    private void setup(final FMLCommonSetupEvent event) {
        theMod = (BuildingGadgets) ModLoadingContext.get().getActiveContainer().getMod();
        CapabilityBlockProvider.register();
        CapabilityTemplate.register();
        PacketHandler.register();
    }

    private void registerRegistries(RegistryEvent.NewRegistry event) {
        Registries.onCreateRegistries();
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

    private void serverLoad(FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(
                Commands.literal(Reference.MODID)
                        .then(OverrideBuildSizeCommand.registerToggle())
                        .then(OverrideCopySizeCommand.registerToggle())
                        .then(ForceUnloadedCommand.registerToggle())
                        .then(OverrideBuildSizeCommand.registerList())
                        .then(OverrideCopySizeCommand.registerList())
                        .then(ForceUnloadedCommand.registerList())
        );
    }

    private void serverLoaded(FMLServerStartedEvent event) {
        SaveManager.INSTANCE.onServerStarted(event);
    }

    private void serverStopped(FMLServerStoppedEvent event) {
        SaveManager.INSTANCE.onServerStopped(event);
    }

    private void onRecipeRegister(final RegistryEvent.Register<IRecipeSerializer<?>> e) {
        e.getRegistry().register(
                Serializer.INSTANCE.setRegistryName(
                    new ResourceLocation(Reference.MODID, "construction_paste")
            )
        );
    }

    private void onEnqueueIMC(InterModEnqueueEvent event) {
        InventoryHelper.registerHandleProviders();
    }
}

package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.api.APIProxy;
import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.commands.BlockMapCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.events.AnvilRepairHandler;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.registry.objects.BuildingObjects;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.command.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.function.Consumer;

@Mod(value = Reference.MODID)
public class BuildingGadgets {

    public static Logger LOG = LogManager.getLogger();
    private static BuildingGadgets theMod = null;
    private final APIProxy theApi;

    public static BuildingGadgets getInstance() {
        assert theMod != null;
        return theMod;
    }

    public BuildingGadgets() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        theApi = new APIProxy(eventBus, MinecraftForge.EVENT_BUS);
        // @todo handle Config properly as soon as Forge fixes it's "I dont load Server Config" bug
        ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_CONFIG);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::serverLoad);
        eventBus.addListener(this::finishLoad);

        MinecraftForge.EVENT_BUS.register(new AnvilRepairHandler());
        MinecraftForge.EVENT_BUS.register(this);

        // Client only registering
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            eventBus.addListener((Consumer<FMLClientSetupEvent>) event -> ClientProxy.clientSetup(event, eventBus));
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> GuiMod::openScreen);

            MinecraftForge.EVENT_BUS.addListener(ClientProxy::registerModels);
        });

        loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("buildinggadgets-client.toml"));
        loadConfig(Config.SERVER_CONFIG, FMLPaths.CONFIGDIR.get().resolve("buildinggadgets-server.toml"));

        BuildingObjects.init();
    }

    private void loadConfig(ForgeConfigSpec spec, Path path) {
        LOG.debug("Loading config file {}", path);
        
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        LOG.debug("Built TOML config for {}", path.toString());
        configData.load();
        LOG.debug("Loaded TOML config file {}", path.toString());
        spec.setConfig(configData);
    }

    private void setup(final FMLCommonSetupEvent event) {
        theMod = (BuildingGadgets) ModLoadingContext.get().getActiveContainer().getMod();
        theApi.onSetup();
        DeferredWorkQueue.runLater(PacketHandler::register);
    }

    private void serverLoad(FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(
                Commands.literal(Reference.MODID)
                    .then(BlockMapCommand.registerList())
                    .then(BlockMapCommand.registerDelete())
        );
    }

    private void finishLoad(FMLLoadCompleteEvent event) {
        theApi.onLoadComplete();
        BuildingObjects.cleanup();
    }

}

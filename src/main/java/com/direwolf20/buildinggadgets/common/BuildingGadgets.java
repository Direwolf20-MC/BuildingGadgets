package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.config.RecipeConstructionPaste.Serializer;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Reference.MODID)
public final class BuildingGadgets {

    public static Logger LOG = LogManager.getLogger();

    /**
     * Register our creative tab. Notice that we're also modifying the NBT data of the
     * building gadget to remove the damage / energy indicator from the creative
     * tabs icon.
     */
    public static CreativeModeTab creativeTab = new CreativeModeTab(Reference.MODID) {
        @Override
        public ItemStack makeIcon() {
            ItemStack stack = new ItemStack(OurItems.BUILDING_GADGET_ITEM.get());
            stack.getOrCreateTag().putByte(NBTKeys.CREATIVE_MARKER, (byte) 0);
            return stack;
        }
    };

    private static BuildingGadgets theMod = null;

    public static BuildingGadgets getInstance() {
        assert theMod != null;
        return theMod;
    }

    public BuildingGadgets() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_CONFIG);

        OurBlocks.BLOCKS.register(eventBus);
        OurItems.ITEMS.register(eventBus);
        OurTileEntities.TILE_ENTITIES.register(eventBus);
        OurContainers.CONTAINERS.register(eventBus);

        MinecraftForge.EVENT_BUS.addListener(this::serverLoad);
        MinecraftForge.EVENT_BUS.addListener(this::serverLoaded);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);

        eventBus.addListener(this::registerRegistries);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::loadComplete);
        eventBus.addListener(this::handleIMC);

        eventBus.addGenericListener(RecipeSerializer.class, this::onRecipeRegister);
        eventBus.addListener(this::onEnqueueIMC);

        eventBus.addListener(this::registerCaps);
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ITemplateProvider.class);
        event.register(ITemplateKey.class);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
//        ClientRegistry.bindTileEntityRenderer(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY.get(), EffectBlockTER::new);
        BlockEntityRenderers.register(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY.get(), EffectBlockTER::new);
        ClientProxy.clientSetup(FMLJavaModLoadingContext.get().getModEventBus());
//        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen);
    }

    private void setup(final FMLCommonSetupEvent event) {
        theMod = (BuildingGadgets) ModLoadingContext.get().getActiveContainer().getMod();

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

    private void serverLoad(ServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(
                Commands.literal(Reference.MODID)
                        .then(OverrideBuildSizeCommand.registerToggle())
                        .then(OverrideCopySizeCommand.registerToggle())
                        .then(ForceUnloadedCommand.registerToggle())
                        .then(OverrideBuildSizeCommand.registerList())
                        .then(OverrideCopySizeCommand.registerList())
                        .then(ForceUnloadedCommand.registerList())
        );
    }

    private void serverLoaded(ServerStartedEvent event) {
        SaveManager.INSTANCE.onServerStarted(event);
    }

    private void serverStopped(ServerStoppedEvent event) {
        SaveManager.INSTANCE.onServerStopped(event);
    }

    private void onRecipeRegister(final RegistryEvent.Register<RecipeSerializer<?>> e) {
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

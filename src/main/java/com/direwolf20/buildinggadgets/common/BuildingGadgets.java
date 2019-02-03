package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.common.blocks.Models.BakedModelLoader;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.commands.BlockMapCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntityRender;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntityRender;
import com.direwolf20.buildinggadgets.common.events.AnvilRepairHandler;
import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tools.ToolRenders;
import net.minecraft.client.Minecraft;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(value = BuildingGadgets.MODID)
public class BuildingGadgets {
    public static final String MODID = "buildinggadgets";
    public static final String MODNAME = "Building Gadgets";
    public static final String VERSION = "@VERSION@";
    public static final String UPDATE_JSON = "@UPDATE@";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.3.2694,)";

    public static Logger LOG = LogManager.getLogger();
    private static BuildingGadgets theMod = null;

    public static BuildingGadgets getInstance() {
        assert theMod != null;
        return theMod;
    }

    private Supplier<Minecraft> mcSupplier;
    public BuildingGadgets() {
        theMod = (BuildingGadgets) FMLModLoadingContext.get().getActiveContainer().getMod();
        mcSupplier = null;
        IEventBus eventBus = FMLModLoadingContext.get().getModEventBus();

        FMLModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_CONFIG);
        FMLModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_CONFIG);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::serverLoad);

        MinecraftForge.EVENT_BUS.register(new AnvilRepairHandler());

        MinecraftForge.EVENT_BUS.register(this);
        // Client only registering
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            eventBus.addListener((Consumer<FMLClientSetupEvent>) (event -> clientInit(event, eventBus)));
        });
    }

    @Nonnull
    public Minecraft getMinecraft() {
        if (mcSupplier == null) throw new RuntimeException("Attempted to access Minecraft instance server Side");
        Minecraft mc = mcSupplier.get();
        assert mc != null;
        return mc;
    }

    private void setup(final FMLCommonSetupEvent event) {
        DeferredWorkQueue.runLater(PacketHandler::register);

// @todo: reimplement @since 1.13.x
//        NetworkRegistry.INSTANCE.registerGuiHandler(BuildingGadgets.instance, new GuiProxy());
    }

    private void clientInit(final FMLClientSetupEvent event, final IEventBus eventBus) {
        mcSupplier = event.getMinecraftSupplier();
        ClientProxy.clientSetup(event, eventBus);
    }

    private void serverLoad(FMLServerStartingEvent event) {

        event.getCommandDispatcher().register(
                Commands.literal(MODID)
                    .then(BlockMapCommand.registerList())
                    .then(BlockMapCommand.registerDelete())
        );

    }

    private static class ClientProxy {

        private static void clientSetup(final FMLClientSetupEvent event, final IEventBus eventBus) {
            DeferredWorkQueue.runLater(() -> {
                KeyBindings.init();
                //            BuildingObjects.initColorHandlers();
            });
            eventBus.addListener(ClientProxy::renderWorldLastEvent);
            eventBus.addListener(ClientProxy::registerSprites);
            MinecraftForge.EVENT_BUS.addListener(ClientProxy::registerModels);
        }

        private static void registerModels(@SuppressWarnings("unused") ModelRegistryEvent event) {
            ModelLoaderRegistry.registerLoader(new BakedModelLoader());

            RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
            RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, new ConstructionBlockEntityRender.Factory());

            // @todo: reimplement? @since 1.13.x
            //        ModBlocks.effectBlock.initModel();
            //        ModBlocks.templateManager.initModel();
            //
            //        BuildingObjects.gadgetBuilding.initModel();
            //        BuildingObjects.gadgetExchanger.initModel();
            //        BuildingObjects.gadgetCopyPaste.initModel();
            //        BuildingObjects.template.initModel();
            //
            //        if (SyncedConfig.enableDestructionGadget) {
            //            BuildingObjects.gadgetDestruction.initModel();
            //        }
            //
            //        if (SyncedConfig.enablePaste) {
            //            BuildingObjects.ConstructionPasteContainer.initModel();
            //            BuildingObjects.constructionPaste.initModel();
            //
            //            ModBlocks.constructionBlock.initModel();
            //            ModBlocks.constructionBlockPowder.initModel();
            //
            //        // REIMPLEMENT
            ////            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainer, new PasteContainerMeshDefinition());
            ////            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainert2, new PasteContainerMeshDefinition());
            ////            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainert3, new PasteContainerMeshDefinition());
            //        }

            RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
            RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, new ConstructionBlockEntityRender.Factory());
        }

        static void renderWorldLastEvent(RenderWorldLastEvent evt) {
            Minecraft mc = Minecraft.getInstance();
            EntityPlayer player = mc.player;
            ItemStack heldItem = GadgetGeneric.getGadget(player);
            if (heldItem.isEmpty()) {
                return;
            }

            if (heldItem.getItem() instanceof GadgetBuilding) {
                ToolRenders.renderBuilderOverlay(evt, player, heldItem);
            } else if (heldItem.getItem() instanceof GadgetExchanger) {
                ToolRenders.renderExchangerOverlay(evt, player, heldItem);
            } else if (heldItem.getItem() instanceof GadgetCopyPaste) {
                ToolRenders.renderPasteOverlay(evt, player, heldItem);
            } else if (heldItem.getItem() instanceof GadgetDestruction) {
                ToolRenders.renderDestructionOverlay(evt, player, heldItem);
            }

        }

        static void registerSprites(TextureStitchEvent.Pre event) {
            registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL);
            registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE);
        }

        private static void registerSprite(TextureStitchEvent.Pre event, String loc) {
            event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), new ResourceLocation(loc));
        }
    }
}

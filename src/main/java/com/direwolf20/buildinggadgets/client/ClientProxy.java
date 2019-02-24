package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.blocks.Models.BakedModelLoader;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy {

    public static void clientSetup(final FMLClientSetupEvent event, final IEventBus eventBus) {
        DeferredWorkQueue.runLater(KeyBindings::init);

        //BuildingObjects.initColorHandlers();
        //eventBus.addListener(ClientProxy::renderWorldLastEvent);
        eventBus.addListener(ClientProxy::registerSprites);
    }

    public static void registerModels(@SuppressWarnings("unused") ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());

        //RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, BlockBuildEntityRender::new);
        //RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, ConstructionBlockEntityRender::new);

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
        //            BuildingObjects.constructionPasteContainerCreative.initModel();
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
    }

    private static void registerSprites(TextureStitchEvent.Pre event) {
        registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL);
        registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE);
    }

    private static void registerSprite(TextureStitchEvent.Pre event, String loc) {
        event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), new ResourceLocation(loc));
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.getMasterRecord(sound, pitch));
    }
}

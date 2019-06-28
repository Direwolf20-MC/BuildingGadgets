package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.events.EventClientTick;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.models.ConstructionBakedModel;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.registry.objects.BGContainers;
import com.direwolf20.buildinggadgets.common.registry.objects.BuildingObjects;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ClientProxy {

    public static void clientSetup(final IEventBus eventBus) {
        DeferredWorkQueue.runLater(KeyBindings::init);
        eventBus.addListener(BuildingObjects::initColorHandlers);
        //eventBus.addListener(ClientProxy::renderWorldLastEvent);
        eventBus.addListener(ClientProxy::bakeModels);
        MinecraftForge.EVENT_BUS.addListener(EventClientTick::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(EventTooltip::onDrawTooltip);
        ScreenManager.registerFactory(BGContainers.TEMPLATE_MANAGER_CONTAINER, (ScreenManager.IScreenFactory<TemplateManagerContainer, TemplateManagerGUI>) TemplateManagerGUI::new);
    }

    private static void bakeModels(ModelBakeEvent event) {
        ResourceLocation ConstrName = new ResourceLocation(Reference.MODID, "construction_block");
        ModelResourceLocation ConstrLocation1 = new ModelResourceLocation(ConstrName, "bright=false,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation2 = new ModelResourceLocation(ConstrName, "bright=true,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation3 = new ModelResourceLocation(ConstrName, "bright=false,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation4 = new ModelResourceLocation(ConstrName, "bright=true,neighbor_brightness=true");
        IDynamicBakedModel constructionBakedModel = new ConstructionBakedModel();
        event.getModelRegistry().put(ConstrLocation1, constructionBakedModel);
        event.getModelRegistry().put(ConstrLocation2, constructionBakedModel);
        event.getModelRegistry().put(ConstrLocation3, constructionBakedModel);
        event.getModelRegistry().put(ConstrLocation4, constructionBakedModel);
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        //registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL);
        //registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE);
    }

    private static void registerSprite(TextureStitchEvent.Pre event, String loc) {
        //TODO replace with something that doesn't result in an StackOverflow error
        //event.getMap().func_215254_a(Minecraft.getInstance().getResourceManager(), Collections.singleton(new ResourceLocation(loc)), null);
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, pitch));
    }
}

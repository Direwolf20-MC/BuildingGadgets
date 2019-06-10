package com.direwolf20.buildinggadgets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ClientProxy {

    public static void clientSetup(final IEventBus eventBus) {
        DeferredWorkQueue.runLater(KeyBindings::init);
        //BuildingObjects.initColorHandlers();
        //eventBus.addListener(ClientProxy::renderWorldLastEvent);
        eventBus.addListener(ClientProxy::registerModels);
    }

    private static void registerModels(@SuppressWarnings("unused") ModelRegistryEvent event) {
        //TODO @since 1.14
        //ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        // @todo: reimplement? @since 1.13.x
        //        if (SyncedConfig.enablePaste) {
        //            ModBlocks.constructionBlock.initModel();
        //        }
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

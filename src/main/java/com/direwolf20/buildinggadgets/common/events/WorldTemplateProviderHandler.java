package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.common.capability.provider.TemplateProviderCapabilityProvider;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Event handler for attaching the TemplateProvider capability to worlds.
 */
@EventBusSubscriber
public final class WorldTemplateProviderHandler {

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject().isClientSide())
            insertProvider(event, ClientProxy.CACHE_TEMPLATE_PROVIDER);
        else
            insertProvider(event, SaveManager.INSTANCE.getTemplateProvider());
    }

    private static void insertProvider(AttachCapabilitiesEvent<Level> event, ITemplateProvider provider) {
        event.addCapability(Reference.WORLD_TEMPLATE_PROVIDER_ID, new TemplateProviderCapabilityProvider(provider));
    }
}

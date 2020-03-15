package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class EventRenderWorldLast {

    @SubscribeEvent
    static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        PlayerEntity player = Minecraft.getInstance().player;
        if( player == null )
            return;

        ItemStack heldItem = AbstractGadget.getGadget(player);
        if (heldItem.isEmpty())
            return;

        ((AbstractGadget) heldItem.getItem()).getRender().render(evt, player, heldItem);
    }

}

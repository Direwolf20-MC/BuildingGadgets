package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class EventRenderWorldLast {

    @SubscribeEvent
    static void RenderLevelLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if( player == null )
            return;

        ItemStack heldItem = AbstractGadget.getGadget(player);
        if (heldItem.isEmpty())
            return;

        ((AbstractGadget) heldItem.getItem()).getRender().render(evt, player, heldItem);
    }

}

package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.tools.ToolRenders;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class EventRenderWorldLast {

    @SubscribeEvent
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
}

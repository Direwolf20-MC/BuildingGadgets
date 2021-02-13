package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.old_items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BuildingGadgetsAPI.MODID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == Phase.START)
            return;

        if (KeyBindings.materialList.isPressed()) {
            GuiMod.MATERIAL_LIST.openScreen(mc.player);
            return;
        }

        ItemStack tool = AbstractGadget.getGadget(mc.player);
        if (tool.isEmpty())
            return;

        KeyBinding mode = KeyBindings.menuSettings;
        if (!(mc.currentScreen instanceof ModeRadialMenu) && mode.isPressed() && ((mode.getKeyModifier() == KeyModifier.NONE
                && KeyModifier.getActiveModifier() == KeyModifier.NONE) || mode.getKeyModifier() != KeyModifier.NONE)) {
                mc.displayGuiScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.range.isPressed()) {
            PacketHandler.sendToServer(new PacketChangeRange());
        } else if (KeyBindings.rotateMirror.isPressed()) {
            PacketHandler.sendToServer(new PacketRotateMirror());
        } else if (KeyBindings.undo.isPressed()) {
            PacketHandler.sendToServer(new PacketUndo());
        } else if (KeyBindings.anchor.isPressed()) {
            PacketHandler.sendToServer(new PacketAnchor());
        } else if (KeyBindings.fuzzy.isPressed()) {
            PacketHandler.sendToServer(new PacketToggleFuzzy());
        } else if (KeyBindings.connectedArea.isPressed()) {
            PacketHandler.sendToServer(new PacketToggleConnectedArea());
        }
    }
}

package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.gui.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketAnchorKey;
import com.direwolf20.buildinggadgets.common.network.packets.PacketChangeRange;
import com.direwolf20.buildinggadgets.common.network.packets.PacketToggleConnectedArea;
import com.direwolf20.buildinggadgets.common.network.packets.PacketToggleFuzzy;
import com.direwolf20.buildinggadgets.common.network.packets.PacketUndoKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@EventBusSubscriber(modid = BuildingGadgets.MODID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == Phase.START)
            return;

        KeyBinding mode = KeyBindings.modeSwitch;
        if (!(mc.currentScreen instanceof ModeRadialMenu) && mode.isPressed() && ((mode.getKeyModifier() == KeyModifier.NONE
                && KeyModifier.getActiveModifier() == KeyModifier.NONE) || mode.getKeyModifier() != KeyModifier.NONE)) {
            ItemStack tool = GadgetGeneric.getGadget(mc.player);
            if (!tool.isEmpty())
                mc.displayGuiScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.rangeChange.isPressed()) {
            PacketHandler.sendToServer(new PacketChangeRange());
        } else if (KeyBindings.undoKey.isPressed()) {
            PacketHandler.sendToServer(new PacketUndoKey());
        } else if (KeyBindings.anchorKey.isPressed()) {
            PacketHandler.sendToServer(new PacketAnchorKey());
        } else if (KeyBindings.fuzzyKey.isPressed()) {
            PacketHandler.sendToServer(new PacketToggleFuzzy());
        } else if (KeyBindings.connectedAreaKey.isPressed()) {
            PacketHandler.sendToServer(new PacketToggleConnectedArea());
        }
    }
}

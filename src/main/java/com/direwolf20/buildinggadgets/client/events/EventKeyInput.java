package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.client.gui.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.*;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == Phase.START)
            return;

        KeyBinding mode = KeyBindings.menuSettings;
        if (!(mc.currentScreen instanceof ModeRadialMenu) && mode.isPressed() && ((mode.getKeyModifier() == KeyModifier.NONE
                && KeyModifier.getActiveModifier() == KeyModifier.NONE) || mode.getKeyModifier() != KeyModifier.NONE)) {
            ItemStack tool = GadgetGeneric.getGadget(mc.player);
            if (!tool.isEmpty())
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
        } else if (KeyBindings.materialList.isPressed()) {
            GuiMod.MATERIAL_LIST.openScreen(mc.player);
        } else if (KeyBindings.printNBT.isPressed()) {
            BuildingGadgets.LOG.debug(Minecraft.getInstance().player.getHeldItemMainhand().getTag());
        }
    }
}

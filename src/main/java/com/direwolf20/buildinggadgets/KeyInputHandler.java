package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.gui.ModeRadialMenu;
import com.direwolf20.buildinggadgets.items.GenericGadget;
import com.direwolf20.buildinggadgets.network.PacketAnchorKey;
import com.direwolf20.buildinggadgets.network.PacketChangeRange;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketUndoKey;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(@SuppressWarnings("unused") InputEvent.KeyInputEvent event) {
        //if (KeyBindings.modeSwitch.isPressed()) {
        if (KeyBindings.modeSwitch.isKeyDown() && ((KeyBindings.modeSwitch.getKeyModifier() == KeyModifier.NONE && KeyModifier.getActiveModifier() == KeyModifier.NONE) || KeyBindings.modeSwitch.getKeyModifier() != KeyModifier.NONE)) {
            //PacketHandler.INSTANCE.sendToServer(new PacketToggleMode());
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (!stack.isEmpty() && ((stack.getItem() instanceof GenericGadget)))
                mc.displayGuiScreen(new ModeRadialMenu(stack));
            else {
                stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
                if (!stack.isEmpty() && ((stack.getItem() instanceof GenericGadget)))
                    mc.displayGuiScreen(new ModeRadialMenu(stack));
            }
        } else if (KeyBindings.rangeChange.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketChangeRange());
        } else if (KeyBindings.undoKey.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketUndoKey());
        } else if (KeyBindings.anchorKey.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketAnchorKey());
        }

    }

    @SubscribeEvent
    public static void onMouseInput(@SuppressWarnings("unused") InputEvent.MouseInputEvent event) {
        //if (KeyBindings.modeSwitch.isPressed()) {
        if (KeyBindings.modeSwitch.isKeyDown() && ((KeyBindings.modeSwitch.getKeyModifier() == KeyModifier.NONE && KeyModifier.getActiveModifier() == KeyModifier.NONE) || KeyBindings.modeSwitch.getKeyModifier() != KeyModifier.NONE)) {
            //PacketHandler.INSTANCE.sendToServer(new PacketToggleMode());
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (!stack.isEmpty() && ((stack.getItem() instanceof GenericGadget)))
                mc.displayGuiScreen(new ModeRadialMenu(stack));
            else {
                stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
                if (!stack.isEmpty() && ((stack.getItem() instanceof GenericGadget)))
                    mc.displayGuiScreen(new ModeRadialMenu(stack));
            }
        } else if (KeyBindings.rangeChange.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketChangeRange());
        } else if (KeyBindings.undoKey.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketUndoKey());
        } else if (KeyBindings.anchorKey.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new PacketAnchorKey());
        }

    }
}
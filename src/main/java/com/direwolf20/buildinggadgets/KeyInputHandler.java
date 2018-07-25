package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.GUIs.ModeRadialMenu;
import com.direwolf20.buildinggadgets.items.GenericGadget;
import com.direwolf20.buildinggadgets.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        //if (KeyBindings.modeSwitch.isPressed()) {
        if (KeyBindings.modeSwitch.isKeyDown()) {
            //PacketHandler.INSTANCE.sendToServer(new PacketToggleMode());
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if(!stack.isEmpty() && (stack.getItem() instanceof GenericGadget))
                mc.displayGuiScreen(new ModeRadialMenu(stack));
            else {
                stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
                if(!stack.isEmpty() && (stack.getItem() instanceof GenericGadget))
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
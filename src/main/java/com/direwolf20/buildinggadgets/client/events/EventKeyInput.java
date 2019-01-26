package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.gui.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.network.PacketAnchorKey;
import com.direwolf20.buildinggadgets.common.network.PacketChangeRange;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketUndoKey;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void onKeyInput(@SuppressWarnings("unused") InputEvent.KeyInputEvent event) {
        handleEventInput();
    }

    @SubscribeEvent
    public static void onMouseInput(@SuppressWarnings("unused") InputEvent.MouseInputEvent event) {
        handleEventInput();
    }

    private static void handleEventInput() {
        if (KeyBindings.modeSwitch.isKeyDown() && ((KeyBindings.modeSwitch.getKeyModifier() == KeyModifier.NONE && KeyModifier.getActiveModifier() == KeyModifier.NONE) || KeyBindings.modeSwitch.getKeyModifier() != KeyModifier.NONE)) {
            //PacketHandler.INSTANCE.sendToServer(new PacketToggleMode());
            Minecraft mc = Minecraft.getInstance();
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (!stack.isEmpty() && ((stack.getItem() instanceof GadgetGeneric)))
                mc.displayGuiScreen(new ModeRadialMenu(stack));
            else {
                stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
                if (!stack.isEmpty() && ((stack.getItem() instanceof GadgetGeneric)))
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

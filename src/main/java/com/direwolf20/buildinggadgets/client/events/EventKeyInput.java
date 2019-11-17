package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.gui.ModeRadialMenu;
import com.direwolf20.buildinggadgets.common.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.network.PacketAnchor;
import com.direwolf20.buildinggadgets.common.network.PacketChangeRange;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.network.PacketToggleConnectedArea;
import com.direwolf20.buildinggadgets.common.network.PacketToggleFuzzy;
import com.direwolf20.buildinggadgets.common.network.PacketUndo;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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
        if (KeyBindings.menuSettings.isKeyDown() && ((KeyBindings.menuSettings.getKeyModifier() == KeyModifier.NONE && KeyModifier.getActiveModifier() == KeyModifier.NONE) || KeyBindings.menuSettings.getKeyModifier() != KeyModifier.NONE)) {
            //PacketHandler.INSTANCE.sendToServer(new PacketToggleMode());
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack tool = GadgetGeneric.getGadget(mc.player);
            if (!tool.isEmpty())
                mc.displayGuiScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.range.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketChangeRange());
        else if (KeyBindings.rotateMirror.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketRotateMirror());
        else if (KeyBindings.undo.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketUndo());
        else if (KeyBindings.anchor.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketAnchor());
        else if (KeyBindings.fuzzy.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketToggleFuzzy());
        else if (KeyBindings.connectedArea.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketToggleConnectedArea());
    }
}

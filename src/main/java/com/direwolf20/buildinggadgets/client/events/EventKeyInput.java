package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.screens.BuildingRadial;
import com.direwolf20.buildinggadgets.client.screens.CopyRadial;
import com.direwolf20.buildinggadgets.client.screens.DestructionRadial;
import com.direwolf20.buildinggadgets.client.screens.ExchangingRadial;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

import static com.direwolf20.buildinggadgets.client.KeyBindings.*;

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
        if (menuSettings.isKeyDown() && ((menuSettings.getKeyModifier() == KeyModifier.NONE && KeyModifier.getActiveModifier() == KeyModifier.NONE) || menuSettings.getKeyModifier() != KeyModifier.NONE)) {
            Minecraft mc = Minecraft.getMinecraft();
            AbstractGadget.getGadget(mc.player).ifPresent(gadget -> {
                if( gadget.getItem() instanceof BuildingGadget )
                    mc.displayGuiScreen(new BuildingRadial(gadget));
                else if( gadget.getItem() instanceof ExchangingGadget)
                    mc.displayGuiScreen(new ExchangingRadial(gadget));
                else if( gadget.getItem() instanceof CopyGadget)
                    mc.displayGuiScreen(new CopyRadial(gadget));
                else if( gadget.getItem() instanceof DestructionGadget)
                    mc.displayGuiScreen(new DestructionRadial(gadget));
            });
        } else if (range.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketChangeRange());
        else if (rotateMirror.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketRotateMirror());
        else if (undo.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketUndo());
        else if (anchor.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketAnchor());
        else if (fuzzy.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketToggleFuzzy());
        else if (connectedArea.isPressed())
            PacketHandler.INSTANCE.sendToServer(new PacketToggleConnectedArea());
    }
}

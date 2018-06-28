package com.direwolf20.buildinggadgets;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.modeSwitch.isPressed()) {
            //NEWPacketHandler.INSTANCE.sendToServer(new PacketToggleMode());
            System.out.println("Mode Switch");
        } else if (KeyBindings.rangeChange.isPressed()) {
            //NEWPacketHandler.INSTANCE.sendToServer(new PacketToggleSubMode());
            System.out.println("Range Change");
        }
    }
}
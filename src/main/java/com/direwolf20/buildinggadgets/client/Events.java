package com.direwolf20.buildinggadgets.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Houses all of our events for the mod. If you need to handle an event, subscribe it here
 * and call out to the right class like {@link Events#KeyInput(InputEvent.KeyInputEvent)} does.
 */
public class Events {

    /**
     * Handles world rendering on the last render pass. Used for in-game rendering
     *
     * @param event the render event
     */
    @SubscribeEvent
    public static void renderWorldLast(RenderWorldLastEvent event) {

    }

    @SubscribeEvent
    public static void keyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if( mc.isGamePaused() || mc.player == null || event.isCanceled() ) {
            return;
        }

        KeyBindings.onKeyPressed(event);
    }
}

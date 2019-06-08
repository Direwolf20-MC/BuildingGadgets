package com.direwolf20.buildinggadgets.client.gui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class BasicGUIBase extends Screen {

    private static final char KEY_CLOSE_GUI = 'e';

    @Override
    public boolean charTyped(char character, int p_charTyped_2_) {
        if (character == KEY_CLOSE_GUI)
            Minecraft.getInstance().player.closeScreen();
        return super.charTyped(character, p_charTyped_2_);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}

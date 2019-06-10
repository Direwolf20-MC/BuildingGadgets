package com.direwolf20.buildinggadgets.client.gui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class BasicGUIBase extends Screen {

    private static final char KEY_CLOSE_GUI = 'e';

    public BasicGUIBase(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    @Override
    public boolean charTyped(char character, int p_charTyped_2_) {
        if (character == KEY_CLOSE_GUI)
            Minecraft.getInstance().player.closeScreen();
        return super.charTyped(character, p_charTyped_2_);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}

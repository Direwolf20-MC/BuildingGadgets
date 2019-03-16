package com.direwolf20.buildinggadgets.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public class GuiButtonHelpText extends GuiButtonAction implements IHoverHelpText {
    protected String helpTextKey;

    public GuiButtonHelpText(int x, int y, int widthIn, int heightIn, String buttonText, String helpTextKey, @Nullable Runnable action) {
        super(x, y, widthIn, heightIn, buttonText, action);
        this.helpTextKey = helpTextKey;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return isMouseOver();
    }

    @Override
    public String getHoverHelpText() {
        return I18n.format(GuiMod.getLangKeyButton("help", helpTextKey));
    }

    @Override
    public void drawRect(Gui gui, int color) {
        drawRect(x, y, x + width, y + height, color);
    }

}

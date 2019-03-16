package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public class GuiButtonHelpText extends GuiButton implements IHoverHelpText {
    protected String helpTextKey;

    public GuiButtonHelpText(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String helpTextKey) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.helpTextKey = helpTextKey;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return isMouseOver();
    }

    @Override
    public String getHoverHelpText() {
        return IHoverHelpText.get("button." + helpTextKey);
    }

    @Override
    public void drawRect(Gui gui, int color) {
        gui.drawRect(x, y, x + width, y + height, color);
    }

}

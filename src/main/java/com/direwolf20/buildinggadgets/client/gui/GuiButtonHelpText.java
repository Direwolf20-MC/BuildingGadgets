package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;

public class GuiButtonHelpText extends Button implements IHoverHelpText {
    protected String helpTextKey;

    public GuiButtonHelpText(int x, int y, int widthIn, int heightIn, String buttonText, String helpTextKey, @Nullable IPressable action) {
        super(x, y, widthIn, heightIn, buttonText, action);
        this.helpTextKey = helpTextKey;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public String getHoverHelpText() {
        return I18n.format(GuiMod.getLangKeyButton("help", helpTextKey));
    }

    @Override
    public void drawRect(AbstractGui gui, int color) {

    }

}

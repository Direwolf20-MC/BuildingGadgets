package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.GuiButton;

public class GuiButtonSelect extends GuiButton {
    protected boolean selected;

    public GuiButtonSelect(int buttonId, int x, int y, int width, int height, String text) {
        super(buttonId, x, y, width, height, text);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelected() {
        selected ^= true;
    }
}
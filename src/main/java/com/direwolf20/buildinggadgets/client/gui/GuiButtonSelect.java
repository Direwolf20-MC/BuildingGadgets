package com.direwolf20.buildinggadgets.client.gui;

import javax.annotation.Nullable;

public class GuiButtonSelect extends GuiButtonAction {
    protected boolean selected;

    public GuiButtonSelect(int x, int y, int width, int height, String text, @Nullable Runnable action) {
        super(x, y, width, height, text, action);
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
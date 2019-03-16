package com.direwolf20.buildinggadgets.client.gui;

public class GuiButtonSelect extends GuiButtonHelpText {
    protected boolean selected;

    public GuiButtonSelect(int buttonId, int x, int y, int width, int height, String text, String helpTextKey) {
        super(buttonId, x, y, width, height, text, helpTextKey);
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
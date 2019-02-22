package com.direwolf20.buildinggadgets.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiButton;

public class GuiButtonAction extends GuiButton {
    private Runnable action;

    public GuiButtonAction(int x, int y, int width, int height, String buttonText, @Nullable Runnable action) {
        super(0, x, y, width, height, buttonText);
        this.action = action;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        performAction();
    }

    public boolean performAction() {
        if (action == null)
            return false;

        action.run();
        return true;
    }
}
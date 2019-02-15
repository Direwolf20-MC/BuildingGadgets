package com.direwolf20.buildinggadgets.client.gui;

import java.awt.*;
import java.util.function.Predicate;

public class GuiButtonAction extends GuiButtonColor {
    private Predicate<Boolean> action;

    public GuiButtonAction(String text, Predicate<Boolean> action) {
        this(0, 0, 0, 0, 0, text, Color.GREEN, Color.LIGHT_GRAY, action);
        selected = action.test(false);
    }

    public GuiButtonAction(int buttonId, int x, int y, int width, int height, String text, Color colorSelected, Color colorDeselected, Predicate<Boolean> action) {
        super(buttonId, x, y, width, height, text, colorSelected, colorDeselected);
        this.action = action;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        action.test(true);
        toggleSelected();
    }
}
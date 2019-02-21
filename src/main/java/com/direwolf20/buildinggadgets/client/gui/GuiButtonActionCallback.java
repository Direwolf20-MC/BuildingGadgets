package com.direwolf20.buildinggadgets.client.gui;

import java.awt.Color;
import java.util.function.Predicate;

public class GuiButtonActionCallback extends GuiButtonColor {
    private Predicate<Boolean> action;

    public GuiButtonActionCallback(String text, Predicate<Boolean> action) {
        this(0, 0, 0, 0, text, Color.GREEN, Color.LIGHT_GRAY, action);
        selected = action.test(false);
    }

    public GuiButtonActionCallback(int x, int y, int width, int height, String text, Color colorSelected, Color colorDeselected, Predicate<Boolean> action) {
        super(x, y, width, height, text, colorSelected, colorDeselected, null);
        this.action = action;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        action.test(true);
        toggleSelected();
    }
}
package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;

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
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        if (pressed) {
            action.test(true);
            toggleSelected();
        }
        return pressed;
    }
}
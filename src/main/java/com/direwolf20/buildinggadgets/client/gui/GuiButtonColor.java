package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;

import java.awt.*;

public class GuiButtonColor extends GuiButtonSound {
    private Color colorSelected, colorDeselected;

    public GuiButtonColor(int buttonId, int x, int y, int width, int height, String text) {
        this(buttonId, x, y, width, height, text, Color.GREEN, Color.LIGHT_GRAY);
    }

    public GuiButtonColor(int buttonId, int x, int y, int width, int height, String text, Color colorSelected, Color colorDeselected) {
        super(buttonId, x, y, width, height, text);
        this.colorSelected = colorSelected;
        this.colorDeselected = colorDeselected;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }

        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        drawRect(x, y, x + width, y + height, (selected ? colorSelected : colorDeselected).getRGB());
        mouseDragged(mc, mouseX, mouseY);
        int textColor = !enabled ? 10526880 : (hovered ? 16777120 : -1);
        mc.fontRenderer.drawString(displayString, x + width / 2 - mc.fontRenderer.getStringWidth(displayString) / 2, y + (height - 8) / 2, textColor);
    }
}
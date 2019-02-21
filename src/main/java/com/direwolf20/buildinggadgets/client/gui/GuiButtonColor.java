package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;

import java.awt.*;

import javax.annotation.Nullable;

public class GuiButtonColor extends GuiButtonSound {
    private Color colorSelected, colorDeselected;

    public GuiButtonColor(int x, int y, int width, int height, String text, @Nullable Runnable action) {
        this(x, y, width, height, text, Color.GREEN, Color.LIGHT_GRAY, action);
    }

    public GuiButtonColor(int x, int y, int width, int height, String text, Color colorSelected, Color colorDeselected, @Nullable Runnable action) {
        super(x, y, width, height, text, action);
        this.colorSelected = colorSelected;
        this.colorDeselected = colorDeselected;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        drawRect(x, y, x + width, y + height, (selected ? colorSelected : colorDeselected).getRGB());
        renderBg(mc, mouseX, mouseY);
        int textColor = !enabled ? 10526880 : (hovered ? 16777120 : -1);
        mc.fontRenderer.drawString(displayString, x + width / 2 - mc.fontRenderer.getStringWidth(displayString) / 2, y + (height - 8) / 2, textColor);
    }
}
package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;

import java.awt.*;

import javax.annotation.Nullable;

public class GuiButtonColor extends GuiButtonSound {
    protected Color colorSelected, colorDeselected, colorHovered;

    public GuiButtonColor(int x, int y, int width, int height, String text, String helpTextKey, @Nullable Runnable action) {
        this(x, y, width, height, text, helpTextKey, Color.GREEN, Color.LIGHT_GRAY, null, action);
    }

    public GuiButtonColor(int x, int y, int width, int height, String text, String helpTextKey, Color colorSelected,
            Color colorDeselected, @Nullable Color colorHovered, @Nullable Runnable action) {
        super(x, y, width, height, text, helpTextKey, action);
        this.colorSelected = colorSelected;
        this.colorDeselected = colorDeselected;
        this.colorHovered = colorHovered;
    }

    protected void setAlpha(int alpha) {
        colorSelected = GuiMod.getColor(colorSelected, alpha);
        colorDeselected = GuiMod.getColor(colorDeselected, alpha);
        colorHovered = GuiMod.getColor(colorHovered, alpha);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        drawRect(x, y, x + width, y + height, (hovered && colorHovered != null ? colorHovered : (selected ? colorSelected : colorDeselected)).getRGB());
        renderBg(mc, mouseX, mouseY);
        if (!displayString.isEmpty()) {
            int textColor = !enabled ? 10526880 : (hovered ? 16777120 : -1);
            mc.fontRenderer.drawString(displayString, x + width / 2 - mc.fontRenderer.getStringWidth(displayString) / 2, y + (height - 8) / 2, textColor);
        }
    }
}
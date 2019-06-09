package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.awt.*;

public class GuiButtonColor extends GuiButtonSound {
    protected Color colorSelected, colorDeselected, colorHovered;

    public GuiButtonColor(int x, int y, int width, int height, String text, String helpTextKey, @Nullable IPressable action) {
        this(x, y, width, height, text, helpTextKey, Color.GREEN, Color.LIGHT_GRAY, null, action);
    }

    public GuiButtonColor(int x, int y, int width, int height, String text, String helpTextKey, Color colorSelected,
                          Color colorDeselected, @Nullable Color colorHovered, @Nullable IPressable action) {
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
        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        drawRect(this, (isHovered && colorHovered != null ? colorHovered : (selected ? colorSelected : colorDeselected)).getRGB());
        renderBg(mc, mouseX, mouseY);
        if (! getMessage().isEmpty()) {
            int textColor = ! active ? 10526880 : (isHovered ? 16777120 : - 1);
            mc.fontRenderer.drawString(getMessage(), x + width / 2 - mc.fontRenderer.getStringWidth(getMessage()) / 2, y + (height - 8) / 2, textColor);
        }
    }
}
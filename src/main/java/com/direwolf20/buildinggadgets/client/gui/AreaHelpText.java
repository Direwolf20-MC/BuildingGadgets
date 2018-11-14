package com.direwolf20.buildinggadgets.client.gui;

import org.lwjgl.util.Rectangle;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Slot;

public class AreaHelpText implements IHoverHelpText {
    private int minX, minY, maxX, maxY;
    private String helpTextKey;

    public AreaHelpText(Rectangle rect, int guiLeft, int guiTop, String helpTextKey) {
        this(guiLeft + rect.getX(), guiTop + rect.getX(), rect.getWidth(), rect.getHeight(), helpTextKey);
    }

    public AreaHelpText(Slot slot, int guiLeft, int guiTop, String helpTextKey) {
        this(guiLeft + slot.xPos, guiTop + slot.yPos, 16, 16, helpTextKey);
    }

    public AreaHelpText(GuiTextField field, String helpTextKey) {
        this(field.x, field.y, field.width, field.height, helpTextKey);
    }

    public AreaHelpText(int x, int y, int width, int height, String helpTextKey) {
        minX = x;
        minY = y;
        maxX = x + width;
        maxY = y + height;
        this.helpTextKey = helpTextKey;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    @Override
    public String getHoverHelpText() {
        return IHoverHelpText.get("area." + helpTextKey);
    }

    @Override
    public void drawRect(Gui gui, int color) {
        gui.drawRect(minX, minY, maxX, maxY, color);
    }

}

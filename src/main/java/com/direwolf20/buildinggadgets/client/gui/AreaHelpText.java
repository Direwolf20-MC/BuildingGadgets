package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.Slot;

public class AreaHelpText implements IHoverHelpText {
    private int minX, minY, maxX, maxY;
    private String helpTextKey;

    public AreaHelpText(Rectangle2d rect, int guiLeft, int guiTop, String helpTextKey) {
        this(guiLeft + rect.getX(), guiTop + rect.getX(), rect.getWidth(), rect.getHeight(), helpTextKey);
    }

    public AreaHelpText(Slot slot, int guiLeft, int guiTop, String helpTextKey) {
        this(guiLeft + slot.xPos, guiTop + slot.yPos, 16, 16, helpTextKey);
    }

    public AreaHelpText(TextFieldWidget field, String helpTextKey) {
        this(field.x, field.y, field.getWidth(), field.getHeight(), helpTextKey);
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
        return I18n.format(GuiMod.getLangKeyArea("help", helpTextKey));
    }

    @Override
    public void drawRect(AbstractGui gui, int color) {
        AbstractGui.fill(minX, minY, maxX, maxY, color);
    }

}

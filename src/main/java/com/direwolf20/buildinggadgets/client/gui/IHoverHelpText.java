package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.AbstractGui;

public interface IHoverHelpText {

    boolean isHovered(int mouseX, int mouseY);

    String getHoverHelpText();

    void drawRect(AbstractGui gui, int color);
}
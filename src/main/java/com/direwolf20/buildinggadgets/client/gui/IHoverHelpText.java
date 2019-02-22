package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.Gui;

public interface IHoverHelpText {

    boolean isHovered(int mouseX, int mouseY);

    String getHoverHelpText();

    void drawRect(Gui gui, int color);
}
package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiTextFieldWithDefault extends GuiTextField {
    private String defaultValue;

    public GuiTextFieldWithDefault(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, String defaultValue) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);

        this.defaultValue = defaultValue;
        this.setMaxStringLength(50);
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}

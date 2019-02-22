package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiTextFieldBase extends GuiTextField {
    private String defaultValue;

    public GuiTextFieldBase(FontRenderer fontRenderer, int x, int y) {
        this(fontRenderer, x, y, 40);
    }

    public GuiTextFieldBase(FontRenderer fontRenderer, int x, int y, int width) {
        super(0, fontRenderer, x, y, width, fontRenderer.FONT_HEIGHT);
        setMaxStringLength(50);
    }

    public GuiTextFieldBase setDefaultInt(int defaultInt) {
        return setDefaultValue(Integer.toString(defaultInt));
    }

    public GuiTextFieldBase setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
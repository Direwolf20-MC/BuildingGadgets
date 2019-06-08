package com.direwolf20.buildinggadgets.client.gui;

import java.util.function.BiConsumer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiTextFieldBase extends TextFieldWidget {
    private boolean suspended;
    private String valueDefault, valueOld;
    private BiConsumer<GuiTextFieldBase, String> postModification;

    public GuiTextFieldBase(FontRenderer fontRenderer, int x, int y, int width) {
        super(0, fontRenderer, x, y, width, fontRenderer.FONT_HEIGHT);
        setMaxStringLength(50);
        setTextAcceptHandler(this::postModification);
        setValidator(s -> {
            valueOld = getText();
            return true;
        });
    }

    public void postModification(Integer id, String text) {
        if (!suspended && postModification != null) {
            suspended = true;
            postModification.accept(this, valueOld);
            suspended = false;
        }
    }

    public GuiTextFieldBase onPostModification(BiConsumer<GuiTextFieldBase, String> postModification) {
        this.postModification = postModification;
        return this;
    }

    public GuiTextFieldBase restrictToNumeric() {
        setValidator(s -> {
            valueOld = getText();
            if (s == null || s.isEmpty() || "-".equals(s))
                return true;

            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        return this;
    }

    public int getInt() {
        try {            
            return Integer.parseInt(getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public GuiTextFieldBase setDefaultInt(int defaultInt) {
        return setDefaultValue(Integer.toString(defaultInt));
    }

    public GuiTextFieldBase setDefaultValue(String defaultValue) {
        this.valueDefault = defaultValue;
        return this;
    }

    public String getDefaultValue() {
        return valueDefault;
    }
}
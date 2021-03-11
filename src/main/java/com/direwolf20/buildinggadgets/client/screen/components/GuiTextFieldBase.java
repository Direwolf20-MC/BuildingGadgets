package com.direwolf20.buildinggadgets.client.screen.components;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.BiConsumer;

public class GuiTextFieldBase extends TextFieldWidget {
    private boolean suspended;
    private String valueDefault, valueOld;
    private BiConsumer<GuiTextFieldBase, String> postModification;

    public GuiTextFieldBase(FontRenderer fontRenderer, int x, int y, int width) {
        super(fontRenderer, x, y, width, 15, StringTextComponent.EMPTY);

        setMaxLength(50);
        setFilter(s -> {
            valueOld = getValue();
            return true;
        });
    }

    @Override
    public void setValue(String textIn) {
        super.setValue(textIn);

        //TODO validate that this is the correct place
        postModification(textIn);
    }

    public void postModification(String text) {
        if (!suspended && postModification != null) {
            suspended = true;
            postModification.accept(this, valueOld);
            suspended = false;
        }
    }

    public GuiTextFieldBase restrictToNumeric() {
        setFilter(s -> {
            valueOld = getValue();
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
            return Integer.parseInt(getValue());
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
}
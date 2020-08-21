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

        setMaxStringLength(50);
        setValidator(s -> {
            valueOld = getText();
            return true;
        });
    }

    @Override
    public void setText(String textIn) {
        super.setText(textIn);

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
}
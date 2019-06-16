package com.direwolf20.buildinggadgets.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;

public class GuiIncrementer extends Widget {
    // this is the width of all components in a line
    public static final int WIDTH = 64;

    private int x;
    private int y;
    private int min;
    private int max;

    private int value;

    private DireButton minusButton;
    private GuiTextFieldBase field;
    private DireButton plusButton;

    public GuiIncrementer(int x, int y, int min, int max) {
        super(x, y, WIDTH, 20, "");

        this.x = x;
        this.y = y;
        this.min = min;
        this.max = max;
        this.value = 0;

        this.minusButton = new DireButton(this.x, this.y - 1, 12, 17, "-", (button) -> this.updateValue(true));
        this.field = new GuiTextFieldBase(Minecraft.getInstance().fontRenderer, x + 13, y, 40).setDefaultInt(this.value).restrictToNumeric();
        this.plusButton = new DireButton(this.x + 40 + 14, this.y - 1, 12, 17, "+", (button) -> this.updateValue(false));

        this.field.setText(String.valueOf(this.value));
    }

    public GuiIncrementer(int x, int y) {
        this(x, y, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int getValue() {
        return this.value;
    }

    private void updateValue(boolean isMinus) {
        int value = isMinus ? this.value - 1 : this.value + 1;
        this.setValue(value);
    }

    public void setValue(int value) {
        if( value > this.max || value < this.min )
            return;

        this.value = value;
        this.field.setText(String.valueOf(this.value));
    }

    public void updateMax(int max) {
        this.max = max;
        if( this.value > max )
            this.setValue(max);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        this.plusButton.render(mouseX, mouseY, partialTick);
        this.minusButton.render(mouseX, mouseY, partialTick);
        this.field.render(mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        this.field.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        this.plusButton.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        this.minusButton.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);

        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if( !this.field.isFocused() )
            return false;

        this.field.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return true;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        if( !this.field.isFocused() )
            return false;

        this.field.charTyped(p_charTyped_1_, p_charTyped_2_);
        if( this.field.getText().length() > 1 && this.field.getText().charAt(0) == '0' )
            this.field.setText(String.valueOf(this.field.getInt()));

        if( this.field.getInt() > this.max )
            this.field.setText(String.valueOf(this.max));

        return true;
    }

    @Override
    protected void onFocusedChanged(boolean isFocused) {
        this.field.changeFocus(isFocused);
        super.onFocusedChanged(isFocused);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

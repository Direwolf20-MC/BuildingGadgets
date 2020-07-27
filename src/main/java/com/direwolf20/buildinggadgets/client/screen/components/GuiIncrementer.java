package com.direwolf20.buildinggadgets.client.screen.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class GuiIncrementer extends Widget {
    // this is the width of all components in a line
    public static final int WIDTH = 64;

    private int x;
    private int y;
    private int min;
    private int max;

    private int value;
    private IIncrementerChanged onChange;

    private DireButton minusButton;
    private GuiTextFieldBase field;
    private DireButton plusButton;

    public GuiIncrementer(int x, int y, int min, int max, @Nullable IIncrementerChanged onChange) {
        super(x, y, WIDTH, 20, StringTextComponent.EMPTY);

        this.x = x;
        this.y = y;
        this.min = min;
        this.max = max;
        this.value = 0;
        this.onChange = onChange;

        this.minusButton = new DireButton(this.x, this.y - 1, 12, 17, new StringTextComponent("-"), (button) -> this.updateValue(true));
        this.field = new GuiTextFieldBase(Minecraft.getInstance().fontRenderer, x + 13, y, 40).setDefaultInt(this.value).restrictToNumeric();
        this.plusButton = new DireButton(this.x + 40 + 14, this.y - 1, 12, 17, new StringTextComponent("+"), (button) -> this.updateValue(false));

        this.field.setText(String.valueOf(this.value));
    }

    public GuiIncrementer(int x, int y) {
        this(x, y, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }

    public int getValue() {
        return this.value;
    }

    private void updateValue(boolean isMinus) {
        int modifier = 1;
        if( Screen.hasShiftDown() )
            modifier *= 10;

        int value = isMinus ? this.value - modifier : this.value + modifier;
        this.setValue(value);
    }

    public void setValue(int value) {
        // We don't want to fire events for no reason
        if( value == this.value )
            return;

        this.value = MathHelper.clamp(value, this.min, this.max);
        this.field.setText(String.valueOf(this.value));

        if( this.onChange != null )
            this.onChange.onChange(value);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTick) {
        this.plusButton.render(matrices, mouseX, mouseY, partialTick);
        this.minusButton.render(matrices, mouseX, mouseY, partialTick);
        this.field.render(matrices, mouseX, mouseY, partialTick);
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

    public interface IIncrementerChanged {
        void onChange(int value);
    }
}

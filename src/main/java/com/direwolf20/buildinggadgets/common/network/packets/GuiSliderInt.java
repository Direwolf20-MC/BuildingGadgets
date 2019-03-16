package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.gui.GuiButtonAction;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.client.config.GuiSlider;

import java.awt.Color;
import java.util.Collection;
import java.util.function.BiConsumer;

public class GuiSliderInt extends GuiSlider {
    private int colorBackground, colorSliderBackground, colorSlider;
    private BiConsumer<GuiSliderInt, Integer> increment;
    private int value;

    public GuiSliderInt(int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal,
            double currentVal, boolean showDec, boolean drawStr, Color color, ISlider par, BiConsumer<GuiSliderInt, Integer> increment) {
        super(0, xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, par);
        colorBackground = GuiMod.getColor(color, 200).getRGB();
        colorSliderBackground = GuiMod.getColor(color.darker(), 200).getRGB();
        colorSlider = GuiMod.getColor(color.brighter().brighter(), 200).getRGB();
        this.increment = increment;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        setValue(getValueInt());
    }

    @Override
    public void updateSlider() {
        super.updateSlider();
        int valueInt = getValueInt();
        if (value != valueInt) {
            value = valueInt;
            playSound();
        }
    }

    private void playSound() {
        ClientProxy.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, 2F);
    }

    @Override
    public void render(int mouseX, int mouseY, float partial) {
        if (!visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        drawRect(x, y, x + width, y + height, colorBackground);
        renderBg(mc, mouseX, mouseY);
        renderText(mc, this);
    }

    private void renderText(Minecraft mc, GuiButton component) {
        int color = !enabled ? 10526880 : (hovered ? 16777120 : -1);
        String buttonText = component.displayString;
        int strWidth = mc.fontRenderer.getStringWidth(buttonText);
        int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
        if (strWidth > component.width - 6 && strWidth > ellipsisWidth)
            buttonText = mc.fontRenderer.trimStringToWidth(buttonText, component.width - 6 - ellipsisWidth).trim() + "...";

        drawCenteredString(mc.fontRenderer, buttonText, component.x + component.width / 2, component.y + (component.height - 8) / 2, color);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {}

    @Override
    protected void renderBg(Minecraft mc, int mouseX, int mouseY) {
        if (!visible)
            return;

        if (dragging) {
            sliderValue = (mouseX - (x + 4)) / (float) (width - 8);
            updateSlider();
        }
        drawBorderedRect(x + (int) (sliderValue * (width - 8)), y, 8, height);
    }

    private void drawBorderedRect(int x, int y, int width, int height) {
        drawRect(x, y, x + width, y + height, colorSliderBackground);
        drawRect(++x, ++y, x + width - 2, y + height - 2, colorSlider); 
    }

    public Collection<GuiButton> getComponents() {
        return ImmutableSet.of(this,
                new GuiButtonIncrement(this, x - height, y, height, height, "-", () -> increment.accept(this, -1)),
                new GuiButtonIncrement(this, x + width, y, height, height, "+", () -> increment.accept(this, 1)));
    }

    private static class GuiButtonIncrement extends GuiButtonAction {
        private GuiSliderInt parent;

        public GuiButtonIncrement(GuiSliderInt parent, int x, int y, int width, int height, String buttonText, Runnable action) {
            super(x, y, width, height, buttonText, action);
            this.parent = parent;
        }

        @Override
        public void render(int mouseX, int mouseY, float partial) {
            if (!visible)
                return;

            Minecraft mc = Minecraft.getInstance();
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            drawRect(x, y, x + width, y + height, parent.colorBackground);
            parent.drawBorderedRect(x, y, width, height);
            parent.renderText(mc, this);
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {}
    }
}

package com.direwolf20.buildinggadgets.client.screen.components;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketChangeRange;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.awt.*;
import java.util.Collection;
import java.util.function.BiConsumer;

public class GuiSliderInt extends Slider {
    private int colorBackground, colorSliderBackground, colorSlider;
    private BiConsumer<GuiSliderInt, Integer> increment;
    private int value;

    public GuiSliderInt(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal,
                        double currentVal, boolean showDec, boolean drawStr, Color color, IPressable par,
                        BiConsumer<GuiSliderInt, Integer> increment) {

        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, par);

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
            PacketHandler.sendToServer(new PacketChangeRange(getValueInt()));
        }
    }

    private void playSound() {
        ClientProxy.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, 2F);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partial) {
        if (!visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        fill(matrices, x, y, x + width, y + height, colorBackground);
        renderBg(matrices, mc, mouseX, mouseY);
        renderText(matrices, mc, this);
    }

    private void renderText(MatrixStack matrices, Minecraft mc, Button component) {
        int color = ! active ? 10526880 : (isHovered ? 16777120 : - 1);
        String buttonText = component.getMessage().getString();
        int strWidth = mc.fontRenderer.getStringWidth(buttonText);
        int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
        if (strWidth > component.getWidth() - 6 && strWidth > ellipsisWidth)
            buttonText = mc.fontRenderer.func_238412_a_(buttonText, component.getWidth() - 6 - ellipsisWidth).trim() + "...";

        drawCenteredString(matrices, mc.fontRenderer, buttonText, component.x + component.getWidth() / 2, component.y + (component.getHeightRealms() - 8) / 2, color);
    }

    @Override
    public void playDownSound(SoundHandler p_playDownSound_1_) { }

    @Override
    protected void renderBg(MatrixStack matrices, Minecraft mc, int mouseX, int mouseY) {
        if (!visible)
            return;

        if (dragging) {
            sliderValue = (mouseX - (x + 4)) / (float) (width - 8);
            updateSlider();
        }

        drawBorderedRect(matrices, x + (int) (sliderValue * (width - 8)), y, 8, height);
    }

    private void drawBorderedRect(MatrixStack matrices, int x, int y, int width, int height) {
        fill(matrices, x, y, x + width, y + height, colorSliderBackground);
        fill(matrices, ++ x, ++ y, x + width - 2, y + height - 2, colorSlider);
    }

    public Collection<Button> getComponents() {
        return ImmutableSet.of(
                this,
                new GuiButtonIncrement(this, x - height, y, height, height, new StringTextComponent("-"), b -> increment.accept(this, - 1)),
                new GuiButtonIncrement(this, x + width, y, height, height, new StringTextComponent("+"), b -> increment.accept(this, 1)
        ));
    }

    private static class GuiButtonIncrement extends Button {
        private GuiSliderInt parent;

        public GuiButtonIncrement(GuiSliderInt parent, int x, int y, int width, int height, ITextComponent buttonText, IPressable action) {
            super(x, y, width, height, buttonText, action);
            this.parent = parent;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float partial) {
            if (!visible)
                return;

            Minecraft mc = Minecraft.getInstance();
            isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            fill(matrices, x, y, x + width, y + height, parent.colorBackground);
            parent.drawBorderedRect(matrices, x, y, width, height);
            parent.renderText(matrices, mc, this);
        }

        @Override
        public void playDownSound(SoundHandler p_playDownSound_1_) { }
    }
}

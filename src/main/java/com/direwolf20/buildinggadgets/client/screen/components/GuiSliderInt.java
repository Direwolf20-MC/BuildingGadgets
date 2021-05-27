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

        this.colorBackground = GuiMod.getColor(color, 200).getRGB();
        this.colorSliderBackground = GuiMod.getColor(color.darker(), 200).getRGB();
        this.colorSlider = GuiMod.getColor(color.brighter().brighter(), 200).getRGB();

        this.increment = increment;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        this.setValue(this.getValueInt());
    }

    @Override
    public void updateSlider() {
        super.updateSlider();
        int valueInt = this.getValueInt();
        if (this.value != valueInt) {
            this.value = valueInt;
            this.playSound();
            PacketHandler.sendToServer(new PacketChangeRange(this.getValueInt()));
        }
    }

    private void playSound() {
        ClientProxy.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, 2F);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partial) {
        if (!this.visible) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, this.colorBackground);
        this.renderBg(matrices, mc, mouseX, mouseY);
        this.renderText(matrices, mc, this);
    }

    private void renderText(MatrixStack matrices, Minecraft mc, Button component) {
        int color = !this.active
            ? 10526880
            : (this.isHovered
                ? 16777120
                : -1);
        String buttonText = component.getMessage().getString();
        int strWidth = mc.fontRenderer.getStringWidth(buttonText);
        int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
        if (strWidth > component.getWidth() - 6 && strWidth > ellipsisWidth) {
            buttonText = mc.fontRenderer.trimStringToWidth(buttonText, component.getWidth() - 6 - ellipsisWidth).trim() + "...";
        }

        drawCenteredString(matrices, mc.fontRenderer, buttonText, component.x + component.getWidth() / 2, component.y + (component.getHeight() - 8) / 2, color);
    }

    @Override
    public void playDownSound(SoundHandler p_playDownSound_1_) {
    }

    @Override
    protected void renderBg(MatrixStack matrices, Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        if (this.dragging) {
            this.sliderValue = (mouseX - (this.x + 4)) / (float) (this.width - 8);
            this.updateSlider();
        }

        this.drawBorderedRect(matrices, this.x + (int) (this.sliderValue * (this.width - 8)), this.y, 8, this.height);
    }

    private void drawBorderedRect(MatrixStack matrices, int x, int y, int width, int height) {
        fill(matrices, x, y, x + width, y + height, this.colorSliderBackground);
        fill(matrices, ++x, ++y, x + width - 2, y + height - 2, this.colorSlider);
    }

    public Collection<Button> getComponents() {
        return ImmutableSet.of(
            this,
            new GuiButtonIncrement(this, this.x - this.height, this.y, this.height, this.height, new StringTextComponent("-"), b -> this.increment.accept(this, -1)),
            new GuiButtonIncrement(this, this.x + this.width, this.y, this.height, this.height, new StringTextComponent("+"), b -> this.increment.accept(this, 1)
            )
        );
    }

    private static class GuiButtonIncrement extends Button {
        private GuiSliderInt parent;

        public GuiButtonIncrement(GuiSliderInt parent, int x, int y, int width, int height, ITextComponent buttonText, IPressable action) {
            super(x, y, width, height, buttonText, action);
            this.parent = parent;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float partial) {
            if (!this.visible) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, this.parent.colorBackground);
            this.parent.drawBorderedRect(matrices, this.x, this.y, this.width, this.height);
            this.parent.renderText(matrices, mc, this);
        }

        @Override
        public void playDownSound(SoundHandler p_playDownSound_1_) {
        }
    }
}

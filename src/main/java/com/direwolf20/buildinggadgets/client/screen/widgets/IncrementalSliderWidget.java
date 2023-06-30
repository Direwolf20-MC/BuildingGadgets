package com.direwolf20.buildinggadgets.client.screen.widgets;

import com.direwolf20.buildinggadgets.client.OurSounds;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.awt.*;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A flat colored, incremental (+ and - buttons) slider widget
 */
public class IncrementalSliderWidget extends ForgeSlider {
    private static final int BACKGROUND = createAlphaColor(Color.DARK_GRAY, 200).getRGB();
    private static final int SLIDER_BACKGROUND = createAlphaColor(Color.DARK_GRAY.darker(), 200).getRGB();
    private static final int SLIDER_COLOR = createAlphaColor(Color.DARK_GRAY.brighter().brighter(), 200).getRGB();

    public final Consumer<IncrementalSliderWidget> onUpdate;

    public IncrementalSliderWidget(int x, int y, int width, int height, double min, double max, Component prefix, double current, Consumer<IncrementalSliderWidget> onUpdate) {
        super(x, y, width, height, prefix, Component.empty(), min, max, current, 1D, 1, true);
        this.onUpdate = onUpdate;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, BACKGROUND);
        this.drawBorderedRect(guiGraphics, (this.getX() + (int)(this.value * (double)(this.width - 8)) + 4) - 4, this.getY(), 8, this.height);
        this.renderText(guiGraphics);
    }

    private void renderText(GuiGraphics guiGraphics) {
        int color = !active ? 10526880 : (isHovered ? 16777120 : -1);

        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawCenteredString(minecraft.font, this.prefix.copy().append(this.getValueString()), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }

    private void drawBorderedRect(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, SLIDER_BACKGROUND);
        guiGraphics.fill(++x, ++y, x + width - 2, y + height - 2, SLIDER_COLOR);
    }

    @Override
    protected void applyValue() {
        this.onUpdate.accept(this);
    }

    @Override
    public void onRelease(double p_93609_, double p_93610_) {
    }

    @Override
    public void playDownSound(SoundManager p_93605_) {
    }

    @Override
    public boolean mouseReleased(double p_93684_, double p_93685_, int p_93686_) {
        var result = super.mouseReleased(p_93684_, p_93685_, p_93686_);

        // Prevents spam of sounds due to the ForgeSlider
        if (result) {
            OurSounds.playSound(OurSounds.BEEP.get());
        }

        return result;
    }

    private static Color createAlphaColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // This is lazy, I should really just build it into a single widget render
    public Collection<AbstractWidget> getComponents() {
        return ImmutableSet.of(
                this,
                new GuiButtonIncrement(getX() - height, getY(), height, height, Component.literal("-"), b -> {
                    this.setValue(this.getValueInt() - 1);
                    IncrementalSliderWidget.this.applyValue();
                }),
                new GuiButtonIncrement(getX() + width, getY(), height, height, Component.literal("+"), b -> {
                    this.setValue(this.getValueInt() + 1);
                    IncrementalSliderWidget.this.applyValue();
                })
        );
    }

    private class GuiButtonIncrement extends Button {
        public GuiButtonIncrement(int x, int y, int width, int height, Component buttonText, OnPress action) {
            super(builder(buttonText, action)
                    .pos(x, y)
                    .size(width, height));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
            if (!visible)
                return;

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;

            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, IncrementalSliderWidget.BACKGROUND);
            IncrementalSliderWidget.this.drawBorderedRect(guiGraphics, this.getX(), this.getY(), this.width, this.height);
            guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        @Override
        public void onRelease(double p_93609_, double p_93610_) {
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            OurSounds.playSound(OurSounds.BEEP.get());
        }
    }
}

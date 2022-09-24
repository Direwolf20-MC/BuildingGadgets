package com.direwolf20.buildinggadgets.client.screen.widgets;

import com.direwolf20.buildinggadgets.client.OurSounds;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, BACKGROUND);
        this.drawBorderedRect(poseStack, (this.x + (int)(this.value * (double)(this.width - 8)) + 4) - 4, this.y, 8, this.height);
        this.renderText(poseStack);
    }

    private void renderText(PoseStack matrices) {
        int color = !active ? 10526880 : (isHovered ? 16777120 : -1);

        Minecraft minecraft = Minecraft.getInstance();
        drawCenteredString(matrices, minecraft.font, this.prefix.copy().append(this.getValueString()), x + getWidth() / 2, y + (getHeight() - 8) / 2, color);
    }

    private void drawBorderedRect(PoseStack matrices, int x, int y, int width, int height) {
        fill(matrices, x, y, x + width, y + height, SLIDER_BACKGROUND);
        fill(matrices, ++x, ++y, x + width - 2, y + height - 2, SLIDER_COLOR);
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
                new GuiButtonIncrement(x - height, y, height, height, Component.literal("-"), b -> {
                    this.setValue(this.getValueInt() - 1);
                    IncrementalSliderWidget.this.applyValue();
                }),
                new GuiButtonIncrement(x + width, y, height, height, Component.literal("+"), b -> {
                    this.setValue(this.getValueInt() + 1);
                    IncrementalSliderWidget.this.applyValue();
                })
        );
    }

    private class GuiButtonIncrement extends Button {
        public GuiButtonIncrement(int x, int y, int width, int height, Component buttonText, OnPress action) {
            super(x, y, width, height, buttonText, action);
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float partial) {
            if (!visible)
                return;

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;

            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, IncrementalSliderWidget.BACKGROUND);
            IncrementalSliderWidget.this.drawBorderedRect(matrices, this.x, this.y, this.width, this.height);
            drawCenteredString(matrices, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
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

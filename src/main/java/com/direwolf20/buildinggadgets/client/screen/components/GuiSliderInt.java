package com.direwolf20.buildinggadgets.client.screen.components;

import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketChangeRange;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.awt.*;
import java.util.Collection;
import java.util.function.BiConsumer;

public class GuiSliderInt extends ForgeSlider {
    private final int colorBackground;
    private final int colorSliderBackground;
    private final int colorSlider;
    private final BiConsumer<GuiSliderInt, Integer> increment;
    private int value;

    private Button.OnPress pressAction;

    public GuiSliderInt(int xPos, int yPos, int width, int height, Component prefix, Component suf, double minVal, double maxVal,
                        double currentVal, Color color, Button.OnPress par,
                        BiConsumer<GuiSliderInt, Integer> increment) {

        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, true);

        this.pressAction = par;
        this.colorBackground = GuiMod.getColor(color, 200).getRGB();
        this.colorSliderBackground = GuiMod.getColor(color.darker(), 200).getRGB();
        this.colorSlider = GuiMod.getColor(color.brighter().brighter(), 200).getRGB();

        this.increment = increment;
    }

    @Override
    protected void applyValue() {
        super.applyValue();
        int valueInt = this.getValueInt();
        if (this.value != valueInt) {
            this.value = valueInt;
            this.playSound();
            PacketHandler.sendToServer(new PacketChangeRange(this.getValueInt()));
        }
    }

    private void playSound() {
        ClientProxy.playSound(SoundEvents.DISPENSER_FAIL, 2F);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partial) {
        if (!visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, this.colorBackground);
        this.renderBg(matrices, mc, mouseX, mouseY);
        this.renderText(matrices, mc, this);
    }

    private void renderText(PoseStack matrices, Minecraft mc, AbstractWidget component) {
        int color = !active ? 10526880 : (isHovered ? 16777120 : -1);
        String buttonText = component.getMessage().getString();
        int strWidth = mc.font.width(buttonText);
        int ellipsisWidth = mc.font.width("...");
        if (strWidth > component.getWidth() - 6 && strWidth > ellipsisWidth)
            buttonText = mc.font.plainSubstrByWidth(buttonText, component.getWidth() - 6 - ellipsisWidth).trim() + "...";

        drawCenteredString(matrices, mc.font, buttonText, component.x + component.getWidth() / 2, component.y + (component.getHeight() - 8) / 2, color);
    }

    @Override
    public void playDownSound(SoundManager p_playDownSound_1_) {
    }

    @Override
    protected void renderBg(PoseStack matrices, Minecraft mc, int mouseX, int mouseY) {
        if (!visible)
            return;

//        if (this.dragging) {
//            this.sliderValue = (mouseX - (this.x + 4)) / (float) (this.width - 8);
//            this.updateSlider();
//        }

        this.drawBorderedRect(matrices, this.x + (int) (this.getValueInt() * (this.width - 8)), this.y, 8, this.height);
    }

    private void drawBorderedRect(PoseStack matrices, int x, int y, int width, int height) {
        fill(matrices, x, y, x + width, y + height, colorSliderBackground);
        fill(matrices, ++x, ++y, x + width - 2, y + height - 2, colorSlider);
    }

    public Collection<AbstractWidget> getComponents() {
        return ImmutableSet.of(
                this,
                new GuiButtonIncrement(this, x - height, y, height, height, Component.literal("-"), b -> increment.accept(this, -1)),
                new GuiButtonIncrement(this, x + width, y, height, height, Component.literal("+"), b -> increment.accept(this, 1)
                ));
    }

    private static class GuiButtonIncrement extends Button {
        private GuiSliderInt parent;

        public GuiButtonIncrement(GuiSliderInt parent, int x, int y, int width, int height, Component buttonText, OnPress action) {
            super(x, y, width, height, buttonText, action);
            this.parent = parent;
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float partial) {
            if (!visible)
                return;

            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, this.parent.colorBackground);
            this.parent.drawBorderedRect(matrices, this.x, this.y, this.width, this.height);
            this.parent.renderText(matrices, mc, this);
        }

        @Override
        public void playDownSound(SoundManager p_playDownSound_1_) {
        }
    }
}

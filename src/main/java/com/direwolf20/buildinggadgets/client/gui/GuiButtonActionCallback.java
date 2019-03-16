package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.function.Predicate;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;

public class GuiButtonActionCallback extends GuiButtonIcon {
    private Predicate<Boolean> action;

    public GuiButtonActionCallback(String name, Predicate<Boolean> action) {
        this(0, 0, 0, 0, String.join(".", "tooltip.gadget", name), Color.GREEN, Color.GRAY, Color.WHITE,
                new ResourceLocation(BuildingGadgets.MODID, String.format("textures/gui/setting/%s.png", name.replace(".", "_"))), action);
        selected = action.test(false);
        setFaded(false);
    }

    public GuiButtonActionCallback(int x, int y, int width, int height, String helpTextKey, Color colorSelected,
            Color colorDeselected, Color colorHovered, ResourceLocation texture, Predicate<Boolean> action) {
        super(x, y, width, height, helpTextKey, colorSelected, colorDeselected, colorHovered, texture, null);
        this.action = action;
    }

    public void setFaded(boolean faded) {
        setAlpha(faded ? 70 : 90);
        setFaded(faded, 70);
    }

    @Override
    public String getHoverHelpText() {
        return I18n.format(helpTextKey);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        if (pressed) {
            action.test(true);
            toggleSelected();
        }
        return pressed;
    }
}
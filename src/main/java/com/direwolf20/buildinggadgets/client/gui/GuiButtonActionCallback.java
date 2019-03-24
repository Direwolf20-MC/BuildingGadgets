package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.function.Predicate;

import com.direwolf20.buildinggadgets.client.gui.ModeRadialMenu.ScreenPosition;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;

public class GuiButtonActionCallback extends GuiButtonIcon {
    private Predicate<Boolean> action;
    private ScreenPosition screenPosition;
    private boolean togglable = true;

    public GuiButtonActionCallback(String name, ScreenPosition screenPosition, Predicate<Boolean> action) {
        this(0, 0, 0, 0, String.join(".", "tooltip.gadget", name), Color.GREEN, Color.GRAY, Color.WHITE,
                new ResourceLocation(BuildingGadgets.MODID, String.format("textures/gui/setting/%s.png", name.replace(".", "_"))), screenPosition, action);
        selected = action.test(false);
        setFaded(false);
    }

    public GuiButtonActionCallback(int x, int y, int width, int height, String helpTextKey, Color colorSelected,
            Color colorDeselected, Color colorHovered, ResourceLocation texture, ScreenPosition screenPosition, Predicate<Boolean> action) {
        super(x, y, width, height, helpTextKey, colorSelected, colorDeselected, colorHovered, texture, null);
        this.screenPosition = screenPosition;
        this.action = action;
    }

    public ScreenPosition getScreenPosition() {
        return screenPosition;
    }

    public GuiButtonActionCallback setTogglable(boolean togglable) {
        this.togglable = togglable;
        return this;
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
            if (togglable)
                toggleSelected();
        }
        return pressed;
    }
}
package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.item.ItemStack;

public class ExchangingRadial extends AbstractRadialMenu {
    private static final ModeIcon[] icons = new ModeIcon[]{
        new ModeIcon("textures/gui/mode/surface.png", "surface"),
        new ModeIcon("textures/gui/mode/vertical_column.png", "vertical_column"),
        new ModeIcon("textures/gui/mode/horizontal_column.png", "horizontal_column"),
        new ModeIcon("textures/gui/mode/grid.png", "grid")
    };

    public ExchangingRadial(ItemStack gadget) {
        super(icons, gadget);
    }
}

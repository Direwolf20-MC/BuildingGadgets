package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.item.ItemStack;

public class BuildingRadial extends AbstractRadialMenu {
    private static final ModeIcon[] icons = new ModeIcon[]{
        new ModeIcon("textures/gui/mode/build_to_me.png", "build_to_me"),
        new ModeIcon("textures/gui/mode/vertical_column.png", "vertical_column"),
        new ModeIcon("textures/gui/mode/horizontal_column.png", "horizontal_column"),
        new ModeIcon("textures/gui/mode/vertical_wall.png", "vertical_wall"),
        new ModeIcon("textures/gui/mode/horizontal_wall.png", "horizontal_wall"),
        new ModeIcon("textures/gui/mode/stairs.png", "stairs"),
        new ModeIcon("textures/gui/mode/grid.png", "grid"),
        new ModeIcon("textures/gui/mode/surface.png", "surface")
    };

    public BuildingRadial(ItemStack gadget) {
        super(icons, gadget);
    }
}

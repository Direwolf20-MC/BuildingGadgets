package com.direwolf20.buildinggadgets.client.gui;

import net.minecraft.item.ItemStack;

public class CopyRadial extends AbstractRadialMenu {
    private static final ModeIcon[] icons = new ModeIcon[]{
        new ModeIcon("textures/gui/mode/copy.png", "copy"),
        new ModeIcon("textures/gui/mode/paste.png", "paste")
    };

    public CopyRadial(ItemStack gadget) {
        super(icons, gadget);
    }
}

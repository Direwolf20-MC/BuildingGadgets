package com.direwolf20.buildinggadgets.client.screens;

import com.direwolf20.buildinggadgets.common.gadgets.CopyGadget;
import net.minecraft.item.ItemStack;

public class CopyRadial extends AbstractRadialMenu {
    private static final ModeIcon[] icons = new ModeIcon[]{
        new ModeIcon("textures/gui/mode/copy.png", "copy"),
        new ModeIcon("textures/gui/mode/paste.png", "paste")
    };

    public CopyRadial(ItemStack gadget) {
        super(icons, gadget);

        this.modeIndex = CopyGadget.getToolMode(gadget).ordinal();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.sortButtons();
    }
}

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class DestructionRadial extends AbstractRadialMenu {
    private static final ResourceLocation[] icons = new ResourceLocation[]{
        new ResourceLocation(BuildingGadgets.MODID, "textures/gui/mode/copy.png"),
        new ResourceLocation(BuildingGadgets.MODID, "textures/gui/mode/paste.png")
    };

    public DestructionRadial(ItemStack gadget) {
        super(icons, gadget);
    }
}

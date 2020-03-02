package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotTemplateManager extends SlotItemHandler {
    private String backgroundLoc;

    public SlotTemplateManager(IItemHandler itemHandler, int index, int xPosition, int yPosition, String backgroundLoc) {
        super(itemHandler, index, xPosition, yPosition);
        this.backgroundLoc = backgroundLoc;
    }

    @Override
    public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        return super.setBackground(atlas, new ResourceLocation(Reference.MODID, this.backgroundLoc));
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import javax.annotation.Nullable;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotTemplateManager extends SlotItemHandler {
    private String backgroundLoc;

    public SlotTemplateManager(IItemHandler itemHandler, int index, int xPosition, int yPosition, String backgroundLoc) {
        super(itemHandler, index, xPosition, yPosition);
        this.backgroundLoc = backgroundLoc;
    }

    @Override
    @Nullable
    public String getSlotTexture() {
        return backgroundLoc;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
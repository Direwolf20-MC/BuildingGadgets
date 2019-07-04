package com.direwolf20.buildinggadgets.common.containers;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

import javax.annotation.Nullable;

public abstract class BaseContainer extends Container {
    public BaseContainer(@Nullable ContainerType<?> p_i50105_1_, int p_i50105_2_) {
        super(p_i50105_1_, p_i50105_2_);
    }

    protected void addPlayerSlots(PlayerInventory playerInventory) {
        // Slots for the hotbar
        for (int row = 0; row < 9; ++ row) {
            int x = 8 + row * 18;
            int y = 58 + 84;
            addSlot(new Slot(playerInventory, row, x, y));
        }
        // Slots for the main inventory
        for (int row = 1; row < 4; ++ row) {
            for (int col = 0; col < 9; ++ col) {
                int x = 8 + col * 18;
                int y = row * 18 + 66;
                addSlot(new Slot(playerInventory, col + row * 9, x, y));
            }
        }
    }
}

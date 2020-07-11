package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.modes.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DestructionGadget extends Gadget {

    public DestructionGadget() {
        super();
    }

    @Override
    public void action() {

    }

    @Override
    public void undo(ItemStack gadget, World world, PlayerEntity player) {

    }

    @Override
    public void undoAction(UndoWorldStore store, UUID uuid, ItemStack gadget, World world, PlayerEntity playerEntity) {

    }

    @Override
    public List<Mode> getModes() {
        return Collections.emptyList();
    }
}

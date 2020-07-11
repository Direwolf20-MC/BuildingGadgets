package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.construction.Mode;
import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.construction.modes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DestructionGadget extends Gadget {
    private static final List<Mode> MODES = Arrays.asList(
            new EmptyMode("custom_area"),
            new HorizontalColumnMode(false),
            new HorizontalWallMode(),
            new VerticalColumnMode(false),
            new VerticalWallMode(),
            new GridMode(false)
    );

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
        return MODES;
    }
}

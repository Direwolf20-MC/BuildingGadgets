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

public class CopierGadget extends Gadget {
    private static final List<Mode> MODES = Arrays.asList(
            new EmptyMode("copy"),
            new EmptyMode("paste")
    );

    public CopierGadget() {
        super();
    }

    @Override
    public void action() {

    }

    @Override
    public List<Mode> getModes() {
        return MODES;
    }

    @Override
    public void undoAction(UndoWorldStore store, UUID uuid, ItemStack gadget, World world, PlayerEntity playerEntity) {}
}

package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.construction.modes.Mode;
import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.construction.modes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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
    public boolean action(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace) {
        return false;
    }

    @Override
    public ActionResult<ItemStack> sneakingAction(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace) {
        return ActionResult.success(gadget);
    }

    @Override
    public List<Mode> getModes() {
        return MODES;
    }

    @Override
    public void undoAction(UndoWorldStore store, UUID uuid, ItemStack gadget, World world, PlayerEntity playerEntity) {}
}

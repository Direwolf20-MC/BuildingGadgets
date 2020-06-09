package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.modes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class ExchangingGadget extends BuildingGadget {
    private static final List<Mode> MODES = Arrays.asList(
        new SurfaceMode(true),
        new GridMode(true),
        new VerticalColumnMode(true),
        new HorizontalColumnMode(true)
    );

    public ExchangingGadget() {
        super();
    }

    @Override
    protected List<Mode> getModes() {
        return MODES;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ActionResult<ItemStack> result = super.onItemRightClick(worldIn, playerIn, handIn);

        return result;
    }
}

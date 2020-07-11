package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.construction.modes.Mode;
import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import com.direwolf20.buildinggadgets.common.construction.modes.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    public List<Mode> getModes() {
        return MODES;
    }

    @Override
    protected void build(World worldIn, PlayerEntity playerIn, ItemStack gadget, @Nullable BlockRayTraceResult rayTrace, BlockState state) {
        if( rayTrace == null ) {
            return;
        }

        // First, get the gadgets mode
        Mode mode = this.getMode(gadget);

        List<BlockPos> blockCollection = mode.getCollection(playerIn, new ModeUseContext(worldIn, state, rayTrace.getPos(), gadget, rayTrace.getFace(), false));
        System.out.println(state);

        blockCollection.forEach(e -> worldIn.setBlockState(e, state));
    }

    /**
     * This gadget can not undo so we disable the action and undo methods
     */
    @Override
    public void undo(ItemStack gadget, World world, PlayerEntity player) {}

    @Override
    public void undoAction(UndoWorldStore store, UUID uuid, ItemStack gadget, World world, PlayerEntity playerEntity) {}
}

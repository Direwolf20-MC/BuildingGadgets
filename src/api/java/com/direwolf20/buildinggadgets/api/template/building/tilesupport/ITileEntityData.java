package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import com.direwolf20.buildinggadgets.api.APIProxy;
import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.abstraction.impl.UniqueItemAdapter;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public interface ITileEntityData {
    ITileDataSerializer getSerializer();

    default boolean allowPlacement(IBlockState state, IWorld world, BlockPos position) {
        return state.isAir(world, position);
    }

    void placeIn(IWorld world, IBlockState state, BlockPos pos);

    default Multiset<IUniqueItem> getRequiredItems(IBlockState state, IWorld world, @Nullable RayTraceResult target, @Nullable BlockPos pos, @Nullable EntityPlayer player) {
        ItemStack stack = null;
        try {
            stack = state.getBlock().getPickBlock(state, target, world, pos, player);
        } catch (Exception e) {
            APIProxy.LOG.trace("Failed to retrieve pickBlock for {}.", state, e);
        }
        if (stack == null) stack = new ItemStack(state.getBlock().asItem());
        if (stack.isEmpty()) return ImmutableMultiset.of();
        return ImmutableMultiset.of(new UniqueItemAdapter(stack.getItem(), stack.getTag()));
    }
}

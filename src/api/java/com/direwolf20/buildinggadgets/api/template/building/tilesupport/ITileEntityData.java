package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import com.direwolf20.buildinggadgets.api.APIProxy;
import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.abstraction.impl.UniqueItemAdapter;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

public interface ITileEntityData {
    ITileDataSerializer getSerializer();

    default boolean allowPlacement(IBuildContext context, IBlockState state, BlockPos position) {
        return state.isAir(context.getWorld(), position);
    }

    boolean placeIn(IBuildContext context, IBlockState state, BlockPos pos);

    default Multiset<IUniqueItem> getRequiredItems(IBuildContext context, IBlockState state, @Nullable RayTraceResult target, @Nullable BlockPos pos) {
        ItemStack stack = null;
        try {
            stack = state.getBlock().getPickBlock(state, target, context.getWorld(), pos, context.getBuildingPlayer());
        } catch (Exception e) {
            APIProxy.LOG.trace("Failed to retrieve pickBlock for {}.", state, e);
        }
        if (stack == null) stack = new ItemStack(state.getBlock().asItem());
        if (stack.isEmpty()) return ImmutableMultiset.of();
        return ImmutableMultiset.of(new UniqueItemAdapter(stack.getItem(), stack.getTag()));
    }
}

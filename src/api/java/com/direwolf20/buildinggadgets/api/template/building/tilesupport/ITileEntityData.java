package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import com.direwolf20.buildinggadgets.api.APIProxy;
import com.direwolf20.buildinggadgets.api.abstraction.UniqueItem;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

/**
 * Represents the serializable data of an {@link net.minecraft.tileentity.TileEntity}. It also provides actions which can be performed on the
 * underlying data, to either:
 * <ul>
 * <li>Check whether placement should be permitted via {@link #allowPlacement(IBuildContext, BlockState, BlockPos)}
 * <li>Place this data with a given {@link BlockState} in an {@link IBuildContext} via {@link #placeIn(IBuildContext, BlockState, BlockPos)}
 * <li>Query the requiredItems to build this {@link ITileEntityData} via {@link #getRequiredItems(IBuildContext, BlockState, RayTraceResult, BlockPos)}.
 * </ul>
 */
public interface ITileEntityData {
    /**
     * @return The {@link ITileDataSerializer} which can be used for serializing this {@link ITileEntityData} instance.
     */
    ITileDataSerializer getSerializer();

    /**
     * Checks whether this {@link ITileEntityData} permits being placed at the given position.
     * @param context The {@link IBuildContext} to place in.
     * @param state The {@link BlockState} to place.
     * @param position The {@link BlockPos} at which to place
     * @return Whether or not placement should be allowed for this {@link ITileEntityData}. Returning false can be as trivial as the position being in the wrong
     *         Biome for this type of liquid for example...
     * @implNote The default implementation checks whether the given position is air.
     */
    default boolean allowPlacement(IBuildContext context, BlockState state, BlockPos position) {
        return context.getWorld().isAirBlock(position);
    }

    /**
     * Attempts to place this {@link ITileEntityData} in the given {@link IBuildContext}. If this is called but {@link #allowPlacement(IBuildContext, BlockState, BlockPos)}
     * would have returned false, placement should still be attempted and counted as a "forced placement".<br>
     * This Method should also set any data on the {@link net.minecraft.tileentity.TileEntity} represented by this {@code ITileEntityData}.
     * @param context The {@link IBuildContext} to place in.
     * @param state The {@link BlockState} to place.
     * @param position The {@link BlockPos} at which to place
     * @return Whether or not placement was performed by this {@link ITileEntityData}. This should only return false if some really hard requirements would not be met,
     *         like for example a required block not being present next to the given position.
     */
    boolean placeIn(IBuildContext context, BlockState state, BlockPos position);

    /**
     * @param context The context in which to query required items.
     * @param state The {@link BlockState} to retrieve items for
     * @param target {@link RayTraceResult} the target at which a click is simulated
     * @param pos The {@link BlockPos} where a block is simulated for this Method
     * @return A {@link Multiset} of required Items.
     */
    default Multiset<UniqueItem> getRequiredItems(IBuildContext context, BlockState state, @Nullable RayTraceResult target, @Nullable BlockPos pos) {
        ItemStack stack = null;
        try {
            stack = state.getBlock().getPickBlock(state, target, context.getWorld(), pos, context.getBuildingPlayer());
        } catch (Exception e) {
            APIProxy.LOG.trace("Failed to retrieve pickBlock for {}.", state, e);
        }
        if (stack == null)
            stack = new ItemStack(state.getBlock().asItem());
        if (stack.isEmpty())
            return ImmutableMultiset.of();
        return ImmutableMultiset.of(new UniqueItem(stack.getItem(), stack.getTag()));
    }
}

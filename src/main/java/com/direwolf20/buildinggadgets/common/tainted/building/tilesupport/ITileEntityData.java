package com.direwolf20.buildinggadgets.common.tainted.building.tilesupport;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.google.common.collect.Multiset;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

/**
 * Represents the serializable data of an {@link net.minecraft.tileentity.TileEntity}. It also provides actions which can be performed on the
 * underlying data, to either:
 * <ul>
 * <li>Check whether placement should be permitted via {@link #allowPlacement(BuildContext, BlockState, BlockPos)}
 * <li>Place this data with a given {@link BlockState} in an {@link BuildContext} via {@link #placeIn(BuildContext, BlockState, BlockPos)}
 * <li>Query the requiredItems to build this {@link ITileEntityData} via {@link #getRequiredItems(BuildContext, BlockState, RayTraceResult, BlockPos)}.
 * </ul>
 */
public interface ITileEntityData {
    /**
     * @return The {@link ITileDataSerializer} which can be used for serializing this {@link ITileEntityData} instance.
     */
    ITileDataSerializer getSerializer();

    /**
     * Attempts to place this {@link ITileEntityData} in the given {@link BuildContext}. If this is called but {@link #allowPlacement(BuildContext, BlockState, BlockPos)}
     * would have returned false, placement should still be attempted and counted as a "forced placement".<br>
     * This Method should also set any data on the {@link net.minecraft.tileentity.TileEntity} represented by this {@code ITileEntityData}.
     * @param context The {@link BuildContext} to place in.
     * @param state The {@link BlockState} to place.
     * @param position The {@link BlockPos} at which to place
     * @return Whether or not placement was performed by this {@link ITileEntityData}. This should only return false if some really hard requirements would not be met,
     *         like for example a required block not being present next to the given position.
     */
    boolean placeIn(BuildContext context, BlockState state, BlockPos position);

    /**
     * @param context The context in which to query required items.
     * @param state The {@link BlockState} to retrieve items for
     * @param target {@link RayTraceResult} the target at which a click is simulated
     * @param pos The {@link BlockPos} where a block is simulated for this Method
     * @return A {@link Multiset} of required Items.
     */
    default MaterialList getRequiredItems(BuildContext context, BlockState state, @Nullable HitResult target, @Nullable BlockPos pos) {
        ItemStack stack = null;
        try {
            // todo: fix
//            stack = state.getBlock().getPickBlock(state, target, context.getWorld(), pos, context.getPlayer());
        } catch (Exception e) {
            BuildingGadgets.LOG.trace("Failed to retrieve pickBlock for {}.", state, e);
        }

        if (stack == null)
            stack = new ItemStack(state.getBlock());

        if (stack.isEmpty())
            return MaterialList.empty();

        return MaterialList.of(UniqueItem.ofStack(stack));
    }
}

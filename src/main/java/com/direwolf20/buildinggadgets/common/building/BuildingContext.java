package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.old_items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BuildingContext {
    /** client world **/
    private final World world;

    /** block we're attempting to change / build to **/
    private final BlockState setState;

    /** Block we're looking at **/
    private final BlockPos startPos;

    /** Face of the block we've hit **/
    private final Direction hitSide;

    /** Gadget settings to avoid constant querying **/
    private final boolean isFuzzy;
    private final boolean placeOnTop;
    private final int range;
    private final boolean rayTraceFluid;
    private final boolean isConnected;

    public BuildingContext(World world, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean placeOnTop, boolean isConnected) {
        this.world = world;
        this.setState = setState;
        this.startPos = startPos;

        this.range = GadgetUtils.getToolRange(gadget);
        this.isFuzzy = AbstractGadget.getFuzzy(gadget);
        this.rayTraceFluid = AbstractGadget.shouldRayTraceFluid(gadget);
        this.hitSide = hitSide;

        this.isConnected = isConnected;
        this.placeOnTop = placeOnTop;
    }

    public BuildingContext(World world, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean isConnected) {
        this(world, setState, startPos, gadget, hitSide, false, isConnected);
    }

    public BlockItemUseContext createBlockUseContext(PlayerEntity player) {
        return new BlockItemUseContext(
            new ItemUseContext(
                player,
                Hand.MAIN_HAND,
                VectorHelper.getLookingAt(player, this.rayTraceFluid)
            )
        );
    }

    public boolean isConnected() {
        return isConnected;
    }

    public BlockState getWorldState(BlockPos pos) {
        return world.getBlockState(pos);
    }

    public World getWorld() {
        return world;
    }

    public BlockState getSetState() {
        return setState;
    }

    public boolean isFuzzy() {
        return isFuzzy;
    }

    public boolean isRayTraceFluid() {
        return rayTraceFluid;
    }

    public boolean isPlaceOnTop() {
        return placeOnTop;
    }

    public int getRange() {
        return range;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public Direction getHitSide() {
        return this.hitSide;
    }

    @Override
    public String toString() {
        return "UseContext{" +
            "world=" + world +
            ", setState=" + setState +
            ", startPos=" + startPos +
            ", hitSide=" + hitSide +
            ", isFuzzy=" + isFuzzy +
            ", placeOnTop=" + placeOnTop +
            ", range=" + range +
            ", rayTraceFluid=" + rayTraceFluid +
            '}';
    }
}

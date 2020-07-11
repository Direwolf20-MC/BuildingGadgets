package com.direwolf20.buildinggadgets.common.construction;

import com.direwolf20.buildinggadgets.common.items.Gadget;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class ModeUseContext {
    private World world;
    private BlockState setState;
    private BlockPos startPos;
    private Direction hitSide;

    private boolean isFuzzy;
    private boolean placeOnTop;
    private int range;
    private boolean rayTraceFluid;

    public ModeUseContext(World world, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean placeOnTop) {
        this.world = world;
        this.setState = setState;
        this.startPos = startPos;

        // TODO: 09/07/2020 this seems stupid to be casting the item to the gadget to then just use the gadget
        this.range = ((Gadget) gadget.getItem()).getRange(gadget);
        this.isFuzzy = true; //AbstractGadget.getFuzzy(gadget);
        this.rayTraceFluid = false;//AbstractGadget.shouldRayTraceFluid(gadget);
        this.hitSide = hitSide;

        this.placeOnTop = placeOnTop;
    }

    public ModeUseContext(World world, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide) {
        this(world, setState, startPos, gadget, hitSide, false);
    }

    public BlockItemUseContext createBlockUseContext(PlayerEntity player) {
        return new BlockItemUseContext(
                new ItemUseContext(
                        player,
                        Hand.MAIN_HAND,
                        (BlockRayTraceResult) player.pick(20, 0, true) //VectorHelper.getLookingAt(player, this.rayTraceFluid)
                )
        );
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

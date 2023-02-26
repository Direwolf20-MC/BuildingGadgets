package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * @implNote I'm 100% sure I could solve the xyz == XYZ.X by using a helper method but I'm happy
 *           with it for now.
 */
public class VerticalWallMode extends AbstractMode {
    public VerticalWallMode() { super(false); }

    @Override
    List<BlockPos> collect(UseContext context, Player player, BlockPos start) {
        int size = (context.getRange() - 1) / 2;

        Direction hitSide = context.getHitSide();
        var side = hitSide.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite().getAxis() : hitSide.getAxis();

        var startY = hitSide.getAxis() == Direction.Axis.Y ? start.getY() : start.getY() - size;
        var endY = hitSide.getAxis() == Direction.Axis.Y ? start.getY() + ((context.getRange() - 1) * (hitSide == Direction.DOWN ? -1 : 1)) : start.getY() + size;

        AABB box = new AABB(
            start.getX() - (side == Direction.Axis.Z ? size : 0), startY, start.getZ() - (side == Direction.Axis.X ? size : 0),
            start.getX() + (side == Direction.Axis.Z ? size : 0), endY, start.getZ() + (side == Direction.Axis.X ? size : 0)
        );

        return BlockPos.betweenClosedStream(box).map(BlockPos::immutable).toList();
    }

    /**
     * We need to modify where the offset is for this mode as when looking at any
     * face that isn't up or down, we need to push the offset back into the block
     * and ignore placeOnTop as this mode does the action by default.
     */
    @Override
    public BlockPos withOffset(UseContext context) {
        return XYZ.isAxisY(context.getHitSide()) ? super.withOffset(context) : context.getStartPos();
    }
}

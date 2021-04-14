package com.direwolf20.buildinggadgets.common.construction;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Mostly used to turn a {@link Direction} into an {@link XYZ} representation.
 * Some common methods have been added to actual do computation based on the result
 * of the XYZ.
 * <p>
 * Most of the cleverness to this class is the transform from Direction to an X, Y or Z.
 * I don't have a better name for this atm, soz :P
 */
public enum XYZ {
    X, Y, Z;

    /**
     * Expands the original block pos by the new offset value. This only happens on a single
     * dimension.
     */
    public static BlockPos extendPosSingle(int value, BlockPos pos, Direction facing, Direction.Axis axis) {
        int change = invertOnFace(facing, value);

        if (axis == Direction.Axis.X) {
            return new BlockPos(pos.getX() + change, pos.getY(), pos.getZ());
        }
        if (axis == Direction.Axis.Y) {
            return new BlockPos(pos.getX(), pos.getY() + change, pos.getZ());
        }

        return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + change);
    }

    /**
     * Inverts the value based on the face. If you're facing in a negative direction
     * the value will be flipped to a negative and vise versa.
     *
     * @param facing Looking at face
     * @param value  Value to be modified
     *
     * @return modified value
     */
    public static int invertOnFace(Direction facing, int value) {
        return value * ((facing == Direction.NORTH || facing == Direction.DOWN || facing == Direction.WEST)
            ? -1
            : 1);
    }
}

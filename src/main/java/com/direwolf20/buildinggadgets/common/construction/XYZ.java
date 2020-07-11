package com.direwolf20.buildinggadgets.common.construction;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Mostly used to turn a {@link Direction} into an {@link XYZ} representation.
 * Some common methods have been added to actual do computation based on the result
 * of the XYZ.
 *
 * Most of the cleverness to this class is the transform from Direction to an X, Y or Z.
 * I don't have a better name for this atm, soz :P
 */
public enum XYZ {
    X, Y, Z;

    public static XYZ fromFacing(Direction facing) {
        if( facing == Direction.SOUTH || facing == Direction.NORTH )
            return XYZ.Z;

        if( facing == Direction.EAST || facing == Direction.WEST )
            return XYZ.X;

        return XYZ.Y;
    }

    public static boolean isAxisX(Direction facing) {
        return facing == Direction.EAST || facing == Direction.WEST;
    }

    public static boolean isAxisY(Direction facing) {
        return facing == Direction.UP || facing == Direction.DOWN;
    }

    public static boolean isAxisZ(Direction facing) {
        return facing == Direction.SOUTH || facing == Direction.NORTH;
    }

    public static int posToXYZ(BlockPos pos, XYZ xyz) {
        if( xyz == X ) return pos.getX();
        if( xyz == Y ) return pos.getY();

        return pos.getZ();
    }

    /**
     * Expands the original block pos by the new offset value. This only happens on a single
     * dimension.
     */
    public static BlockPos extendPosSingle(int value, BlockPos pos, Direction facing, XYZ xyz) {
        int change = invertOnFace(facing, value);

        if( xyz == X ) return new BlockPos(pos.getX() + change, pos.getY(), pos.getZ());
        if( xyz == Y ) return new BlockPos(pos.getX(), pos.getY() + change, pos.getZ());

        return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + change);
    }

    /**
     * Inverts the value based on the face. If you're facing in a negative direction
     * the value will be flipped to a negative and vise versa.
     *
     * @param facing Looking at face
     * @param value  Value to be modified
     * @return modified value
     */
    public static int invertOnFace(Direction facing, int value) {
        return value * ((facing == Direction.NORTH || facing == Direction.DOWN || facing == Direction.WEST) ? -1 : 1);
    }
}

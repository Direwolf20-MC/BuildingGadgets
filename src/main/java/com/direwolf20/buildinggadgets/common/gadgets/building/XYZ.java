package com.direwolf20.buildinggadgets.common.gadgets.building;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.Sys;

/**
 * Mostly used to turn a {@link EnumFacing} into an {@link XYZ} representation.
 * Some common methods have been added to actual do computation based on the result
 * of the XYZ.
 *
 * Most of the cleverness to this class is the transform from EnumFacing to an X, Y or Z.
 * I don't have a better name for this atm, soz :P
 */
public enum XYZ {
    X, Y, Z;

    public static XYZ fromFacing(EnumFacing facing) {
        if( facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH )
            return XYZ.Z;

        if( facing == EnumFacing.EAST || facing == EnumFacing.WEST )
            return XYZ.X;

        return XYZ.Y;
    }

    public static int posToXYZ(BlockPos pos, XYZ xyz) {
        if( xyz == X ) return pos.getX();
        if( xyz == Y ) return pos.getY();

        return pos.getZ();
    }

    /**
     * Very similar to {@link #extendPosDouble(int, int, BlockPos, XYZ)} as it expands
     * the original block pos by the new offset value. This only happens on a single
     * dimension though.
     */
    public static BlockPos extendPosSingle(int value, BlockPos pos, EnumFacing facing, XYZ xyz) {
        int change = value * ((facing == EnumFacing.NORTH || facing == EnumFacing.DOWN || facing == EnumFacing.WEST) ? -1 : 1);

        if( xyz == X ) return new BlockPos(pos.getX() + change, pos.getY(), pos.getZ());
        if( xyz == Y ) return new BlockPos(pos.getX(), pos.getY() + change, pos.getZ());

        return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + change);
    }

    /**
     * Used offset / grow a blockPos depending on the face you're looking at.
     * You pipe in the original block and the offset values (a, b) that you want
     * to shift the block by.
     *
     * todo: fix math
     *
     * @param a a can be either the new x,y,z
     * @param b b can be either the new x,y,z
     * @param pos block pos to build from
     * @param facingXYZ XYZ
     * @return BlockPos
     */
    public static BlockPos extendPosDouble(int a, int b, BlockPos pos, XYZ facingXYZ) {
        if( facingXYZ == X ) return new BlockPos(pos.getX() + a, pos.getY() + b, pos.getZ());
        if( facingXYZ == Y ) return new BlockPos(pos.getX() + a, pos.getY(), pos.getZ() + b);

        return new BlockPos(pos.getX(), pos.getY() + a, pos.getZ() + b);
    }
}

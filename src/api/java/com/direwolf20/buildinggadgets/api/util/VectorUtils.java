package com.direwolf20.buildinggadgets.api.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public final class VectorUtils {
    private VectorUtils() {}

    public static int getAxisValue(BlockPos pos, Direction.Axis axis) {
        switch (axis) {
            case X:
                return pos.getX();
            case Y:
                return pos.getY();
            case Z:
                return pos.getZ();
        }
        throw new IllegalArgumentException("Trying to find the value an imaginary axis of a BlockPos");
    }

    public static int getAxisValue(int x, int y, int z, Direction.Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        throw new IllegalArgumentException("Trying to find the value an imaginary axis of a set of 3 values");
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, Direction intersector, int i, int j) {
        return perpendicularSurfaceOffset(pos, intersector.getAxis(), i, j);
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, Direction.Axis intersector, int i, int j) {
        switch (intersector) {
            case X:
                return pos.add(0, i, j);
            case Y:
                return pos.add(i, 0, j);
            case Z:
                return pos.add(i, j, 0);
        }
        throw new IllegalArgumentException("Unknown facing " + intersector);
    }
}

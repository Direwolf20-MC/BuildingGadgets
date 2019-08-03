package com.direwolf20.buildinggadgets.api.util;

import net.minecraft.util.math.BlockPos;

public final class MathUtils {
    public static final int B1_BYTE_MASK = 0xFF;
    public static final int B2_BYTE_MASK = 0xFF_FF;
    public static final int B3_BYTE_MASK = 0xFF_FF_FF;
    private MathUtils() {}

    public static short additiveInverse(short num) {
        return (short) - num;
    }

    /**
     * Converts the BlockPos to a long. Under the assumption, that it is non-negative and does not exceed [0, 255] for the y Coordinate
     * and [0, 65536] for x and z‬ Coordinates.
     */
    public static long posToLong(BlockPos pos) {
        long res = (long) (pos.getX() & B2_BYTE_MASK) << 24;
        res |= (pos.getY() & B1_BYTE_MASK) << 16; // y-Positions are in [0,255] inclusive
        res |= (pos.getZ() & B2_BYTE_MASK);
        return res;
    }

    public static BlockPos posFromLong(long serialized) {
        int x = (int) ((serialized >> 24) & B2_BYTE_MASK);
        int y = (int) ((serialized >> 16) & B1_BYTE_MASK);
        int z = (int) (serialized & B2_BYTE_MASK);
        return new BlockPos(x, y, z);
    }

    public static long includeStateId(long serialized, int id) {
        return serialized | ((long) (id & B3_BYTE_MASK) << 40);
    }

    public static int readStateId(long serialized) {
        return (int) ((serialized >> 40) & B3_BYTE_MASK);
    }

    public static int floorMultiple(int i, int factor) {
        return i - (i % factor);
    }

    public static int ceilMultiple(int i, int factor) {
        return i + (i % factor);
    }

    public static boolean isEven(int i) {
        return (i & 1) == 0;
    }

    public static boolean isOdd(int i) {
        return i % 2 == 1;
    }

    private static int addForNonEven(int i, int c) {
        return isEven(i) ? i : i + c;
    }

    private static int addForNonOdd(int i, int c) {
        return isOdd(i) ? i : i + c;
    }

    public static int floorToEven(int i) {
        return addForNonEven(i, -1);
    }

    public static int floorToOdd(int i) {
        return addForNonOdd(i, -1);
    }

    public static int ceilToEven(int i) {
        return addForNonEven(i, 1);
    }

    public static int ceilToOdd(int i) {
        return addForNonOdd(i, 1);
    }
}

package com.direwolf20.buildinggadgets.api.utils;

import net.minecraft.util.math.BlockPos;

public final class MathUtils {
    private static final int SHORT_BYTE_MASK = 0xFFFF;
    private static final int BYTE_BYTE_MASK = 0xFF;
    private MathUtils() {}

    public static short additiveInverse(short num) {
        return (short) - num;
    }

    public static long posToLong(BlockPos pos) {
        long res = (long) (pos.getX() & SHORT_BYTE_MASK) << 24;
        res |= (pos.getY() & BYTE_BYTE_MASK) << 16; //y-Positions are in [0,255] inclusive
        res |= (pos.getZ() & SHORT_BYTE_MASK);
        return res;
    }

    public static BlockPos posFromLong(long serialized) {
        int x = (int) ((serialized >> 24) & SHORT_BYTE_MASK);
        int y = (int) ((serialized >> 16) & SHORT_BYTE_MASK);
        int z = (int) (serialized & SHORT_BYTE_MASK);
        return new BlockPos(x, y, z);
    }

    public static long includeStateId(long serialized, int id) {
        return serialized | ((long) (id & 0xFFFFFF) << 40);
    }

    public static int readStateId(long serialized) {
        return (int) ((serialized >> 40) & 0xFFFFFF);
    }
}

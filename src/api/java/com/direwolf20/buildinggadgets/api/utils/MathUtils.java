package com.direwolf20.buildinggadgets.api.utils;

import net.minecraft.util.math.BlockPos;

public final class MathUtils {
    private static final int SHORT_BYTE_MASK = 0xFFFF;
    private MathUtils() {}

    public static short additiveInverse(short num) {
        return (short) - num;
    }

    public static long posToLong(BlockPos pos) {
        long res = (long) (pos.getX() & SHORT_BYTE_MASK) << 32;
        res |= (pos.getY() & SHORT_BYTE_MASK) << 16;
        res |= (pos.getZ() & SHORT_BYTE_MASK);
        return res;
    }

    public static BlockPos posFromLong(long serialized) {
        int x = (int) ((serialized >> 32) & SHORT_BYTE_MASK);
        int y = (int) ((serialized >> 16) & SHORT_BYTE_MASK);
        int z = (int) (serialized & SHORT_BYTE_MASK);
        return new BlockPos(x, y, z);
    }

    public static long includeStateId(long serialized, short id) {
        return serialized | ((long) id << 48);
    }

    public static short readStateId(long serialized) {
        return (short) (serialized >> 48);
    }
}

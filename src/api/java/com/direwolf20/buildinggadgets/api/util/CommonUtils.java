package com.direwolf20.buildinggadgets.api.util;

import net.minecraft.util.math.Vec3i;

import java.util.Comparator;

public final class CommonUtils {
    private CommonUtils() {}

    public static final Comparator<Vec3i> POSITION_COMPARATOR = Comparator
            .comparingInt(Vec3i::getX)
            .thenComparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getZ);
}

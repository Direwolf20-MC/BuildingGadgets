package com.direwolf20.buildinggadgets.client.renderer;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Comparator;

public enum SphereSegmentation {
    HIGH_SEGMENTATION(64, 8 * 8),
    MEDIUM_HIGH_SEGMENTATION(48, 16 * 16),
    MEDIUM_SEGMENTATION(32, 24 * 24),
    LOW_SEGMENTATION(16, 32 * 32);
    public static final Comparator<SphereSegmentation> BY_DISTANCE = Comparator.comparing(SphereSegmentation::getMaxSquareDist);
    public static final ImmutableList<SphereSegmentation> VALUES = ImmutableList.sortedCopyOf(BY_DISTANCE, Arrays.asList(values()));

    private final int segments;
    private final int maxDist;

    SphereSegmentation(int segments, int maxDist) {
        this.segments = segments;
        this.maxDist = maxDist;
    }

    public int getSegments() {
        return segments;
    }

    private int getMaxSquareDist() {
        return maxDist;
    }

    public static SphereSegmentation forSquareDist(double dist) {
        for (int i = 0; i < VALUES.size() - 1; i++) {
            SphereSegmentation value = VALUES.get(i);
            if (value.getMaxSquareDist() <= dist)
                return value;
        }
        return VALUES.get(VALUES.size() - 1);
    }
}

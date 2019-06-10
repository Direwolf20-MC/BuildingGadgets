package com.direwolf20.buildinggadgets.test.util;

import com.direwolf20.buildinggadgets.api.util.MathUtils;

import java.util.Random;

public class ShortRandom {
    private Random random;

    public ShortRandom() {
        this(new Random());
    }

    public ShortRandom(Random random) {
        this.random = random;
    }

    public short nextShort() {
        return nextShort(Short.MAX_VALUE);
    }

    public short nextShortIncludingNegative() {
        return nextShortIncludingNegative(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public short nextShort(short bound) {
        return (short) random.nextInt(bound);
    }

    public short nextShortIncludingNegative(short bound) {
        return nextShortIncludingNegative(MathUtils.additiveInverse(bound), bound);
    }

    public short nextShortIncludingNegative(short lower, short upper) {
        int val = random.nextInt(upper - lower);
        return (short) (val + lower);
    }
}

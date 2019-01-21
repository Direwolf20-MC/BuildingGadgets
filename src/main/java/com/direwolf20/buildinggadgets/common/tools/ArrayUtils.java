package com.direwolf20.buildinggadgets.common.tools;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class ArrayUtils {
    public static ShortList deepCastToShort(int[] ar) {
        ShortList list = new ShortArrayList(ar.length);
        for (int i : ar) {
            list.add((short) i);
        }
        return list;
    }
}

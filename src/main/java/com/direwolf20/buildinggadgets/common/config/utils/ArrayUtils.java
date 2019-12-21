package com.direwolf20.buildinggadgets.common.config.utils;

/**
 * Utility class providing various Methods for boxing and unboxing arrays
 *
 * todo: remove in 1.14
 */
public class ArrayUtils {
    public static byte[] asPrimitive(Byte[] ar) {
        byte[] res = new byte[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static int[] asPrimitive(Integer[] ar) {
        int[] res = new int[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static char[] asPrimitive(Character[] ar) {
        char[] res = new char[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static double[] asPrimitive(Double[] ar) {
        double[] res = new double[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static Byte[] asBoxed(byte[] ar) {
        Byte[] res = new Byte[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static Integer[] asBoxed(int[] ar) {
        Integer[] res = new Integer[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static Character[] asBoxed(char[] ar) {
        Character[] res = new Character[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }

    public static Double[] asBoxed(double[] ar) {
        Double[] res = new Double[ar.length];
        for (int i = 0; i < ar.length; ++i) {
            res[i] = ar[i];
        }
        return res;
    }
}

package com.direwolf20.buildinggadgets.common.tools;

public final class MathTool {

    private MathTool() {}

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

package com.direwolf20.buildinggadgets.common.tools;

public final class MathTool {

    private MathTool() {}

    public static boolean isEven(int i) {
        return (i & 1) == 0;
    }

    public static boolean isOdd(int i) {
        return !isEven(i);
    }

}

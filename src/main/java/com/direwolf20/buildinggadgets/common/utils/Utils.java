package com.direwolf20.buildinggadgets.common.utils;

public class Utils {
    public static boolean isStringNumeric(String str)
    {
        for (char c : str.toCharArray())
            if (!Character.isDigit(c)) return false;

        return true;
    }
}

package com.direwolf20.buildinggadgets.common.utils.lang;

import com.direwolf20.buildinggadgets.common.utils.ref.Reference;

public class LangUtil {
    public static String getLangKeyPrefix(String type, String... args) {
        return getLangKey(type, args) + ".";
    }

    public static String getLangKey(String type, String... args) {
        return String.join(".", type, Reference.MODID, String.join(".", args));
    }
}

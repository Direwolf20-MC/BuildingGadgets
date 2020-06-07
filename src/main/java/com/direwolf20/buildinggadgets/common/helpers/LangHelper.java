package com.direwolf20.buildinggadgets.common.helpers;

import com.direwolf20.buildinggadgets.BuildingGadgets;

public final class LangHelper {

    public static String key(String keyEnding) {
        return String.format("%s:%s", BuildingGadgets.MOD_ID, keyEnding);
    }
}

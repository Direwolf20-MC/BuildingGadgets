package com.direwolf20.buildinggadgets.common.helpers;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.BuildingGadget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public final class LangHelper {

    public static ResourceLocation fromKey(String keyEnding) {
        return new ResourceLocation(BuildingGadgets.MOD_ID, keyEnding);
    }

    public static String key(String key, String keyEnding) {
        return String.format("%s.%s.%s", key, BuildingGadgets.MOD_ID, keyEnding);
    }

    public static TranslationTextComponent compMessage(String group, String key, Object... args) {
        return new TranslationTextComponent(key(group, key), args);
    }

    public static TranslationTextComponent compMessage(String group, String key) {
        return new TranslationTextComponent(key(group, key));
    }
}

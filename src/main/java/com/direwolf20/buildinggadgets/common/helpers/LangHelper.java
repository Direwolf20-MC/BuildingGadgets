package com.direwolf20.buildinggadgets.common.helpers;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public final class LangHelper {

    public static ResourceLocation fromKey(String keyEnding) {
        return new ResourceLocation(BuildingGadgets.MOD_ID, keyEnding);
    }

    public static String key(String keyEnding) {
        return String.format("%s:%s", BuildingGadgets.MOD_ID, keyEnding);
    }

    public static TranslationTextComponent compMessage(String key, Object... args) {
        return new TranslationTextComponent(key(String.format("message.%s", key)), args);
    }

    public static TranslationTextComponent compMessage(String key) {
        return new TranslationTextComponent(key(String.format("message.%s", key)));
    }
}

package com.direwolf20.buildinggadgets.common.util.lang;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public interface ITranslationProvider {
    /*Client side only! */
    default String format(Object... args) {
        assert areValidArguments(args);
        return I18n.format(getTranslationKey(), args);
    }

    default TranslationTextComponent componentTranslation(Object... args) {
        assert areValidArguments(args);
        return new TranslationTextComponent(getTranslationKey(), args);
    }

    boolean areValidArguments(Object... args);

    String getTranslationKey();
}

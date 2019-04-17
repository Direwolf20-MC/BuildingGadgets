package com.direwolf20.buildinggadgets.common.utils.lang;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

public interface ITranslationProvider {
    /*Client side only! */
    default String format(Object... args) {
        assert areValidArguments(args);
        return I18n.format(getTranslationKey(), args);
    }

    default TextComponentTranslation componentTranslation(Object... args) {
        assert areValidArguments(args);
        return new TextComponentTranslation(getTranslationKey(), args);
    }

    boolean areValidArguments(Object... args);

    String getTranslationKey();
}

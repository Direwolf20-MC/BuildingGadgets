package com.direwolf20.buildinggadgets.common.util.lang;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface ITranslationProvider {
    /*Client side only! */
    default String format(Object... args) {
        assert areValidArguments(args);
        return I18n.get(getTranslationKey(), args);
    }

    default MutableComponent componentTranslation(Object... args) {
        assert areValidArguments(args);
        return Component.translatable(getTranslationKey(), args);
    }

    boolean areValidArguments(Object... args);

    String getTranslationKey();
}

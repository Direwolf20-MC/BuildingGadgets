package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnull;

public enum ModeTranslation implements ITranslationProvider{
    COPY("copy"),
    PASTE("paste");

    private static final String PREFIX = Reference.MODID + ".modes.";
    private final String key;

    ModeTranslation(@Nonnull String key) {
        this.key = PREFIX + key;
    }

    @Override
    public boolean areValidArguments(Object... args) {
        return true;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}

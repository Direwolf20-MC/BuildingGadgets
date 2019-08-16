package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum MessageTranslation implements ITranslationProvider {
    COPY_UNLOADED("copy_unloaded", 1);
    private static final String PREFIX = Reference.MODID + ".message.";
    private final String key;
    private final int argCount;

    MessageTranslation(@Nonnull String key, @Nonnegative int argCount) {
        this.key = PREFIX + key;
        this.argCount = argCount;
    }

    @Override
    public boolean areValidArguments(Object... args) {
        return args.length == argCount;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}

package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum CommandTranslation implements ITranslationProvider {
    COPY_UNLOADED_NO_PLAYER("copy_unloaded.no_player", 0),
    COPY_UNLOADED_TOGGLED("copy_unloaded.toggled", 2),
    COPY_UNLOADED_LIST("copy_unloaded.list", 2);
    private static final String PREFIX = Reference.MODID + ".commands.";
    private final String key;
    private final int argCount;

    CommandTranslation(@Nonnull String key, @Nonnegative int argCount) {
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

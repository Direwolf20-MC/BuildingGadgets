package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum GuiTranslation implements ITranslationProvider {
    SINGLE_CONFIRM("single.confirm"),
    SINGLE_CANCEL("single.cancel"),
    SINGLE_CLOSE("single.close"),
    SINGLE_CLEAR("single.clear"),

    COPY_BUTTON_ABSOLUTE("copy.button.absolute");

    private static final String PREFIX = "gui." + Reference.MODID + ".";
    private final String key;
    private final int argCount;

    GuiTranslation(@Nonnull String key, @Nonnegative int argCount) {
        this.key = PREFIX + key;
        this.argCount = argCount;
    }

    GuiTranslation(@Nonnull String key) {
        this.key = PREFIX + key;
        this.argCount = 0;
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

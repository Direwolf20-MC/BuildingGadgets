package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum  MaterialListTranslation implements ITranslationProvider{
    BUTTON_CLOSE("button.close", 0),
    BUTTON_REFRESH("button.refresh", 0),
    BUTTON_SORTING_NAMEAZ("button.sorting.nameAZ", 0),
    BUTTON_SORTING_NAMEZA("button.sorting.nameZA", 0),
    BUTTON_SORTING_REQUIREDACSE("button.sorting.requiredAcse", 0),
    BUTTON_SORTING_REQUIREDESC("button.sorting.requiredDesc", 0),
    BUTTON_SORTING_MISSINGACSE("button.sorting.missingAcse", 0),
    BUTTON_SORTING_MISSINGDESC("button.sorting.missingDesc", 0),
    TITLE("title", 0);
    private static final String PREFIX = "gui."+ Reference.MODID+".materialList.";
    private final String key;
    private final int argCount;

    MaterialListTranslation(@Nonnull String key, @Nonnegative int argCount) {
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

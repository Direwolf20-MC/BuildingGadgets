package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum ModeTranslation implements ITranslationProvider{
    SURFACE("surface", 0),
    GRID("grid", 0),
    HORIZONTAL_COLUMN("horizontal_column", 0),
    VERTICAL_COLUMN("vertical_column", 0),
    HORIZONTAL_WALL("horizontal_wall", 0),
    VERTICAL_WALL("vertical_wall", 0),
    STAIR("stair", 0),
    AXIS_CHASING("axis_chasing", 0);
    private static final String PREFIX = "modes."+ Reference.MODID+".";
    private final String key;
    private final int argCount;

    ModeTranslation(@Nonnull String key, @Nonnegative int argCount) {
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

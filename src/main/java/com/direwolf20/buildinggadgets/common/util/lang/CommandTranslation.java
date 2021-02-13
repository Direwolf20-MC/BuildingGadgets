package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum CommandTranslation implements ITranslationProvider {
    FORCE_UNLOADED_NO_PLAYER("force_unloaded.no_player", 0),
    FORCE_UNLOADED_TOGGLED("force_unloaded.toggled", 2),
    FORCE_UNLOADED_LIST("force_unloaded.list", 2),
    OVERRIDE_COPY_SIZE_NO_PLAYER("override_copy_size.no_player", 0),
    OVERRIDE_COPY_SIZE_TOGGLED("override_copy_size.toggled", 2),
    OVERRIDE_COPY_SIZE_LIST("override_copy_size.list", 2),
    OVERRIDE_BUILD_SIZE_NO_PLAYER("override_build_size.no_player", 0),
    OVERRIDE_BUILD_SIZE_TOGGLED("override_build_size.toggled", 2),
    OVERRIDE_BUILD_SIZE_LIST("override_build_size.list", 2);
    private static final String PREFIX = BuildingGadgetsAPI.MODID + ".commands.";
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

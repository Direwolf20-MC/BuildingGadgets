package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum MessageTranslation implements ITranslationProvider {
    COPY_UNLOADED("copy_unloaded", 1),
    AREA_COPIED("copied", 0),
    AREA_COPIED_FAILED("not_copied", 0),
    AREA_COPIED_FAILED_TOO_BIG("area_too_big", 0),
    AREA_COPIED_FAILED_TOO_MANY("too_many_blocks", 0),
    AREA_COPIED_FAILED_TOO_MANY_DIFF("too_many_dif_blocks", 0),
    TEMPLATE_BUILD("template_build", 0),
    GADGET_BUSY("gadget_busy", 0),
    SERVER_BUSY("server_busy", 0),
    NOTHING_TO_UNDO("nothing_to_undo", 0),
    UNDO_FAILED("undo_failed", 0);
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

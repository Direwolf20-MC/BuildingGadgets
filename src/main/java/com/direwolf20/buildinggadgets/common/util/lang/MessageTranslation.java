package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum MessageTranslation implements ITranslationProvider {
    AREA_COPIED("copied", 0),
    AREA_COPIED_FAILED("not_copied", 0),
    AREA_COPIED_FAILED_TOO_BIG("area_too_big", 0),
    AREA_COPIED_FAILED_TOO_MANY("too_many_blocks", 0),
    AREA_COPIED_FAILED_TOO_MANY_DIFF("too_many_dif_blocks", 0),
    ANCHOR_REMOVED("anchor_removed", 0),
    ANCHOR_SET("anchor_set", 0),
    COPY_UNLOADED("copy_unloaded", 1),
    TEMPLATE_BUILD("template_build", 0),
    TRANSACTION_FAILED("transaction_failed", 0),
    GADGET_BUSY("gadget_busy", 0),
    SERVER_BUSY("server_busy", 0),
    NOTHING_TO_UNDO("nothing_to_undo", 0),
    MIRRORED("mirrored", 0),
    ROTATED("rotated", 0),
    RANGE_SET("range_set", 1),
    UNDO_FAILED("undo_failed", 0),
    UNDO_UNLOADED("undo_unloaded", 1),
    UNDO_MISSING_ITEMS("undo_missing_items", 0);
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

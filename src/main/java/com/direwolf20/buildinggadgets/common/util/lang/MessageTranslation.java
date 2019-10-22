package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum MessageTranslation implements ITranslationProvider {
    AREA_COPIED("copied"),
    AREA_COPIED_FAILED("not_copied"),
    AREA_COPIED_FAILED_TOO_BIG("area_too_big"),
    AREA_COPIED_FAILED_TOO_MANY("too_many_blocks"),
    AREA_COPIED_FAILED_TOO_MANY_DIFF("too_many_dif_blocks"),
    AREA_RESET("area_reset"),
    ANCHOR_REMOVED("anchor_removed"),
    ANCHOR_SET("anchor_set"),
    BUILD_UNLOADED("build_unloaded", 1),
    BUILD_TOO_LARGE("build_too_large", 6),
    CLIPBOARD_COPY_SUCCESS("copy_clipboard_success", 0),
    CLIPBOARD_COPY_ERROR("copy_failed.error", 0),
    CLIPBOARD_COPY_ERROR_TEMPLATE("copy_failed.template_write", 0),
    COPY_UNLOADED("copy_unloaded", 1),
    COPY_TOO_LARGE("copy_too_large", 6),
    DESTRCUT_TOO_LARGE("destroy_size_too_large"),
    GADGET_BUSY("gadget_busy"),
    INVALID_BLOCK("invalid_block", 1),
    MIRRORED("mirrored"),
    MODE_SET("tool_mode", 1),
    NOTHING_TO_UNDO("nothing_to_undo"),
    PASTE_FAILED("paste_failed", 0),
    PASTE_FAILED_LINK_COPIED("paste_failed.link_copied", 0),
    PASTE_FAILED_WRONG_MC_VERSION("paste_failed.wrong_mc_version", 2),
    PASTE_FAILED_TOO_RECENT_VERSION("paste_failed.too_recent_version", 2),
    PASTE_FAILED_CORRUPT_JSON("paste_failed.corrupt_json", 0),
    PASTE_FAILED_INVALID_JSON("paste_failed.invalid_json", 0),
    PASTE_FAILED_CORRUPT_BODY("paste_failed.corrupt_body", 0),
    PASTE_SUCCESS("paste_success", 0),
    ROTATED("rotated"),
    TEMPLATE_BUILD("template_build"),
    TRANSACTION_FAILED("transaction_failed"),
    SERVER_BUSY("server_busy"),
    RANGE_SET("range_set", 1),
    UNDO_FAILED("undo_failed"),
    UNDO_UNLOADED("undo_unloaded", 1),
    UNDO_MISSING_ITEMS("undo_missing_items");

    private static final String PREFIX = Reference.MODID + ".message.";
    private final String key;
    private final int argCount;

    MessageTranslation(@Nonnull String key) {
        this(key, 0);
    }

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

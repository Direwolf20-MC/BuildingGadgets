package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public enum GuiTranslation implements ITranslationProvider {
    SINGLE_CONFIRM("single.confirm"),
    SINGLE_CANCEL("single.cancel"),
    SINGLE_CLOSE("single.close"),
    SINGLE_CLEAR("single.clear"),
    SINGLE_RESET("single.reset"),
    SINGLE_LEFT("destruction.field.left"),
    SINGLE_RIGHT("destruction.field.right"),
    SINGLE_UP("destruction.field.up"),
    SINGLE_DOWN("destruction.field.down"),
    SINGLE_DEPTH("destruction.field.depth"),
    SINGLE_RANGE("single.range"),

    BUTTON_LOAD("tm.button.load"),
    BUTTON_SAVE("tm.button.save"),
    BUTTON_COPY("tm.button.copy"),
    BUTTON_PASTE("tm.button.paste"),

    TEMPLATE_NAME_TIP("tm.name_field.text"),
    TEMPLATE_PLACEHOLDER("tm.field.placeholder"),

    COPY_BUTTON_ABSOLUTE("copy.button.absolute"),
    COPY_LABEL_HEADING("copy.label.heading"),
    COPY_LABEL_SUBHEADING("copy.label.subheading"),

    FIELD_START("field.start"),
    FIELD_END("field.end");

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

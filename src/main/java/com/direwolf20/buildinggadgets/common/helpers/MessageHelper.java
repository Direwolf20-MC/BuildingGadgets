package com.direwolf20.buildinggadgets.common.helpers;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.block.Block;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public final class MessageHelper {

    public static String translationKey(String key, String keyEnding) {
        return String.format("%s.%s.%s", key, BuildingGadgets.MOD_ID, keyEnding);
    }

    public static TranslationTextComponent translation(String group, String key, Object... args) {
        return new TranslationTextComponent(translationKey(group, key), args);
    }

    public static String blockName(Block block) {
        return block.getName().getString();
    }

    public static Builder builder(String group, String key, Object... args) {
        return new Builder(group, key, args);
    }

    public static class Builder {
        private final TranslationTextComponent component;

        public Builder(String group, String key, Object... args) {
            component = new TranslationTextComponent(
                    translationKey(group, key),
                    args
            );
        }

        public Builder style(Style style) {
            component.setStyle(style);
            return this;
        }

        public Builder error() {
            component.setStyle(component.getStyle().applyFormat(TextFormatting.RED).withBold(true));
            return this;
        }

        public Builder success() {
            component.setStyle(component.getStyle().applyFormat(TextFormatting.GREEN).withBold(true));
            return this;
        }

        public Builder info() {
            component.setStyle(component.getStyle().applyFormat(TextFormatting.AQUA));
            return this;
        }

        public TranslationTextComponent build() {
            return component;
        }
    }
}

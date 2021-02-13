package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public final class LangUtil {
    private LangUtil() {}

    public static String getLangKey(String type, String... args) {
        return String.join(".", type, BuildingGadgetsAPI.MODID, String.join(".", args));
    }

    public static String getFormattedBlockName(BlockState block) {
        return getFormattedBlockName(block.getBlock());
    }

    public static String getFormattedBlockName(Block block) {
        return block.getTranslatedName().getString();
    }

}

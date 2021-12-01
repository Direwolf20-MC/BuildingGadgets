package com.direwolf20.buildinggadgets.common.util.lang;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class LangUtil {
    private LangUtil() {}

    public static String getLangKey(String type, String... args) {
        return String.join(".", type, Reference.MODID, String.join(".", args));
    }

    public static String getFormattedBlockName(BlockState block) {
        return getFormattedBlockName(block.getBlock());
    }

    public static String getFormattedBlockName(Block block) {
        return block.getName().getString();
    }

}

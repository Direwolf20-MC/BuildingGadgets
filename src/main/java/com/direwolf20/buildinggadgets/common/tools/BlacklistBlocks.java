package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.Config;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlacklistBlocks {
    private static ArrayList<String> blacklistedBlocks = new ArrayList<String>();

    @Nonnull
    private static ResourceLocation getName(Block block) {
        ResourceLocation name = block.getRegistryName();
        if (name == null)
            throw new IllegalArgumentException("A registry name for the following block could not be found: " + block);

        return name;
    }

    private static void addBlockToBlacklist(Block block) {
        blacklistedBlocks.add(getName(block).toString());
    }

    private static void addStringToBlacklist(String name) {
        blacklistedBlocks.add(name);
    }

    private static void setConfig(Configuration cfg, List<String> blacklist) {
        String[] tempArray = new String[blacklist.size()];
        blacklist.toArray(tempArray);
        cfg.get(Config.CATEGORY_BLACKLIST, "Blacklist", tempArray);
    }

    public static boolean checkBlacklist(Block block) {
        return blacklistedBlocks.contains(getName(block).toString());
    }

    public static void getBlacklist(Configuration cfg) {
        ConfigCategory category = cfg.getCategory(Config.CATEGORY_BLACKLIST);
        if (category.isEmpty()) {
            addBlockToBlacklist(Blocks.OAK_DOOR);
            addBlockToBlacklist(Blocks.BIRCH_DOOR);
            addBlockToBlacklist(Blocks.ACACIA_DOOR);
            addBlockToBlacklist(Blocks.DARK_OAK_DOOR);
            addBlockToBlacklist(Blocks.IRON_DOOR);
            addBlockToBlacklist(Blocks.JUNGLE_DOOR);
            addBlockToBlacklist(Blocks.SPRUCE_DOOR);
            addBlockToBlacklist(Blocks.PISTON_HEAD);
            setConfig(cfg, blacklistedBlocks);
        } else{
            for (String entry : category.get("Blacklist").getStringList()) {
                addStringToBlacklist(entry);
            }
        }
    }
}

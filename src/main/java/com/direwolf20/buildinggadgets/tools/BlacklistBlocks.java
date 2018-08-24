package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.Config;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;

public class BlacklistBlocks {
    protected static ArrayList<String> blacklistedBlocks = new ArrayList<String>();

    public static void addBlockToBlacklist(Block block) {
        blacklistedBlocks.add(block.getRegistryName().toString());
    }

    public static void addStringToBlacklist(String name) {
        blacklistedBlocks.add(name);
    }

    public static void setConfig(Configuration cfg, ArrayList<String> blacklist) {
        String[] tempArray = new String[blacklist.size()];
        blacklist.toArray(tempArray);
        cfg.get(Config.CATEGORY_BLACKLIST, "Blacklist", tempArray);
    }

    public static boolean checkBlacklist(Block block) {
        return blacklistedBlocks.contains(block.getRegistryName().toString());
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

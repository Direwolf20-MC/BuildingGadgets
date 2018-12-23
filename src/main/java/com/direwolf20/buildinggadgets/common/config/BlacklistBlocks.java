package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class BlacklistBlocks {
    private static List<Block> blockBlacklist = ImmutableList.of();

    @Nonnull
    static String getName(Block block) {
        ResourceLocation name = block.getRegistryName();
        if (name == null)
            throw new IllegalArgumentException("A registry name for the following block could not be found: " + block);

        return name.toString();
    }

    static void parseBlackList(String[] cfgBlacklist) {
        ImmutableList.Builder<Block> blockBuilder = new Builder<>();
        for (String blockName:cfgBlacklist) {
            ResourceLocation regName = new ResourceLocation(blockName);
            Block block = ForgeRegistries.BLOCKS.getValue(regName);
            if (block!=null)
                blockBuilder.add(block);
            else
                BuildingGadgets.logger.error("Failed to add {} (={}) to Block-Blacklist as Block is unknown to the Game!",blockName,regName);
        }
        blockBlacklist = blockBuilder.build();
    }

    public static boolean checkBlacklist(Block block) {
        return blockBlacklist.contains(block);
    }

}

package com.direwolf20.buildinggadgets.common.construction;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.block.Blocks.*;

/**
 * Defines what is not allowed to be selected or built. Pretty simple class but it's good to
 * have all this in one place.
 *
 * @todo: add IMC
 */
public class BlockAuthority {
    public static final List<Block> BANNED_BLOCKS = Arrays.asList(
            BEDROCK, END_PORTAL, NETHER_PORTAL, END_GATEWAY, BARRIER
    );

    public static final List<Class<? extends Block>> BANNED_BLOCK_CLASSES = Arrays.asList(
        BedBlock.class, DoorBlock.class
    );

    public static boolean allowed(BlockState state) {
        return !BANNED_BLOCKS.contains(state.getBlock()) && !BANNED_BLOCK_CLASSES.contains(state.getBlock().getClass());
    }
}

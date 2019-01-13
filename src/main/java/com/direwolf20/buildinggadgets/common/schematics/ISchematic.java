package com.direwolf20.buildinggadgets.common.schematics;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public interface ISchematic{
    public int getWidth();

    public int getHeight();

    public int getLength();

    public List<Block> getBlocks();

    public default Block getBlock(BlockPos pos) {
        return getBlock(pos.getX(),pos.getY(),pos.getZ());
    }

    public Block getBlock(int x, int y, int z);
}

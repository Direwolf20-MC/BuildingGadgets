package com.direwolf20.buildinggadgets.common.gadgets.building;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMode {
    abstract List<BlockPos> collect(BlockPos start, BlockPos playerPos, EnumFacing side);

    /**
     * Gets the collection with filters applied stopping us having to handle the filters in the actual collection
     * method from having to handle the world etc.
     */
    public List<BlockPos> getCollection(World world, IBlockState setBlock, BlockPos start, BlockPos playerPos, EnumFacing side, boolean placeOnTop) {
        BlockPos startPos = placeOnTop ? start.offset(side, 1) : start;

        return collect(startPos, playerPos, side)
                .stream()
                .filter(e -> isReplaceable(world, e, setBlock))
                .collect(Collectors.toList());
    }

    private static boolean isReplaceable(World world, BlockPos pos, IBlockState setBlock) {
        if (!setBlock.getBlock().canPlaceBlockAt(world, pos))
            return false;

        if (world.isOutsideBuildHeight(pos))
            return false;

        return SyncedConfig.canOverwriteBlocks ? world.getBlockState(pos).getBlock().isReplaceable(world, pos) : world.getBlockState(pos).getMaterial() != Material.AIR;
    }
}

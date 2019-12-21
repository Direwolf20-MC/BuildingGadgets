package com.direwolf20.buildinggadgets.common.gadgets.building;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMode {
    private boolean isExchanging;

    public AbstractMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    abstract List<BlockPos> collect(EntityPlayer player, BlockPos playerPos, EnumFacing side, int range, BlockPos start);

    /**
     * Gets the collection with filters applied stopping us having to handle the filters in the actual collection
     * method from having to handle the world etc.
     */
    public List<BlockPos> getCollection(EntityPlayer player, World world, IBlockState setBlock, BlockPos start, BlockPos playerPos, EnumFacing side, int range, boolean placeOnTop, boolean isFuzzy) {
        BlockPos startPos = this.withOffset(start, side, placeOnTop);

        // We don't need this unless we're using the exchanger but I also don't want to
        // have to remake the state for every block.
        IBlockState lookingAtState = isExchanging() ? world.getBlockState(startPos) : null;

        // We alternate the validator as the exchanger requires a more in-depth validation process.
        return collect(player, playerPos, side, range, startPos)
                .stream()
                .filter(e -> isExchanging ? this.exchangingValidator(world, e, lookingAtState, setBlock, isFuzzy) : this.validator(world, e, start, setBlock, isFuzzy))
                .collect(Collectors.toList());
    }

    /**
     * This method does the barest minimum checking that is needed by most modes
     *
     * @param world         the world
     * @param pos           the block pos we're validating against
     * @param lookingAt     the block we're looking at (mostly unused in the Building Gadget modes)
     * @param setBlock      the block we're setting to
     * @param isFuzzy       if the gadget is fuzzy (mostly unused in the Building Gadget modes)
     *
     * @return if the block is valid
     */
    public boolean validator(World world, BlockPos pos, BlockPos lookingAt, IBlockState setBlock, boolean isFuzzy) {
        if (!setBlock.getBlock().canPlaceBlockAt(world, pos))
            return false;

        if (world.isOutsideBuildHeight(pos))
            return false;

        return SyncedConfig.canOverwriteBlocks ? world.getBlockState(pos).getBlock().isReplaceable(world, pos) : world.getBlockState(pos).getMaterial() != Material.AIR;
    }

    public boolean exchangingValidator(World world, BlockPos pos, IBlockState lookingAtState, IBlockState setBlock, boolean isFuzzy) {
        IBlockState worldBlockState = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);

        if ((worldBlockState != lookingAtState && !isFuzzy)
                || worldBlockState == ModBlocks.effectBlock.getDefaultState()
                || worldBlockState == setBlock )
            return false;

        if (te != null && (!(te instanceof ConstructionBlockTileEntity) || ((ConstructionBlockTileEntity) te).getBlockState() == setBlock))
            return false;

        if (worldBlockState.getBlockHardness(world, pos) < 0)
            return false;

        if( worldBlockState.getMaterial() == Material.AIR || worldBlockState.getMaterial().isLiquid() )
            return false;

        // Finally, ensure at least a single face is exposed.
        boolean hasSingeValid = false;
        for(EnumFacing direction : EnumFacing.values()) {
            BlockPos offset = pos.offset(direction);
            if( !world.getBlockState(offset).doesSideBlockRendering(world, pos, direction.getOpposite()) ) {
                hasSingeValid = true;
                break;
            }
        }

        return hasSingeValid;
    }

    public BlockPos withOffset(BlockPos pos, EnumFacing side, boolean placeOnTop) {
        return placeOnTop ? pos.offset(side, 1) : pos;
    }

    public boolean isExchanging() {
        return isExchanging;
    }
}

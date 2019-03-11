package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionBlockPowder extends BlockFalling {

    public ConstructionBlockPowder() {
        super(Material.SAND);
        BlockModBase.init(this, 0.5F, "constructionblockpowder");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        BlockModBase.initModel(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add(I18n.format("tooltip.constructionblockpowder.helptext"));
    }

    @Override
    public void onEndFalling(World worldIn, BlockPos pos, IBlockState p_176502_3_, IBlockState p_176502_4_) {
        if (p_176502_4_.getMaterial().isLiquid()) {
            worldIn.spawnEntity(new ConstructionBlockEntity(worldIn, pos, true));
        }
    }

    private boolean tryTouchWater(World worldIn, BlockPos pos) {
        boolean flag = false;

        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (enumfacing != EnumFacing.DOWN) {
                BlockPos blockpos = pos.offset(enumfacing);

                if (worldIn.getBlockState(blockpos).getMaterial() == Material.WATER) {
                    flag = true;
                    break;
                }
            }
        }

        if (flag) {
            if (worldIn.getEntitiesWithinAABB(ConstructionBlockEntity.class, new AxisAlignedBB(pos.getX()-0.5, pos.getY()-0.5, pos.getZ()-0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).isEmpty()) {
                worldIn.spawnEntity(new ConstructionBlockEntity(worldIn, pos, true));
            }
        }

        return flag;
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.tryTouchWater(worldIn, pos)) {
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        }
    }

    /**
     * Called after the block is set in the Chunk data, but before the Tile Entity is set
     */
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.tryTouchWater(worldIn, pos)) {
            super.onBlockAdded(worldIn, pos, state);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState p_isFullBlock_1_) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

}

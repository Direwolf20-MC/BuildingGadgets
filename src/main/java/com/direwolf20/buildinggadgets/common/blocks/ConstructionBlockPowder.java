package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.utils.lang.TooltipTranslation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionBlockPowder extends BlockFalling {
    public ConstructionBlockPowder(Properties builder) {
        super(builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TooltipTranslation.CONSTRUCTIONBLOCKPOWDER_HELPTEXT.componentTranslation());
    }

    @Override
    public void onEndFalling(World worldIn, BlockPos pos, IBlockState p_176502_3_, IBlockState p_176502_4_) {
        if (worldIn.getFluidState(pos).isTagged(FluidTags.WATER))
            worldIn.spawnEntity(new ConstructionBlockEntity(worldIn, pos, true));
    }

    private boolean tryTouchWater(IWorld worldIn, BlockPos pos) {
        boolean foundWater = false;

        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (enumfacing != EnumFacing.DOWN && worldIn.getFluidState(pos.offset(enumfacing)).isTagged(FluidTags.WATER)) {
                foundWater = true;
                break;
            }
        }

        if (foundWater) {
            if (worldIn.getWorld().getEntitiesWithinAABB(ConstructionBlockEntity.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).isEmpty()) {
                worldIn.spawnEntity(new ConstructionBlockEntity(worldIn.getWorld(), pos, true));
            }
        }

        return foundWater;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        tryTouchWater(world, pos);
    }

    @Override
    public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
        if (!this.tryTouchWater(worldIn, pos)) {
            super.onBlockAdded(state, worldIn, pos, oldState);
        }
    }

    /**
     * Return true if the block is a normal, solid cube.  This
     * determines indirect power state, entity ejection from blocks, and a few
     * others.
     *
     * @param state The current state
     * @param world The current world
     * @param pos   Block position in world
     * @return True if the block is a full cube
     */
    @Override
    public boolean isNormalCube(IBlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public int getOpacity(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

}

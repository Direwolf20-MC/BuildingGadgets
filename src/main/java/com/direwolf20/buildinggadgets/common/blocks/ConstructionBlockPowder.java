package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionBlockPowder extends FallingBlock {
    public ConstructionBlockPowder() {
        super(Block.Properties.create(Material.SAND).hardnessAndResistance(0.5F).harvestTool(ToolType.SHOVEL));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TooltipTranslation.CONSTRUCTIONBLOCKPOWDER_HELPTEXT.componentTranslation());
    }

    @Override
    public void onEndFalling(World worldIn, BlockPos pos, BlockState p_176502_3_, BlockState p_176502_4_, FallingBlockEntity p_176502_5_) {
        if (worldIn.getFluidState(pos).isTagged(FluidTags.WATER))
            worldIn.addEntity(new ConstructionBlockEntity(worldIn, pos, true));
    }

    private boolean tryTouchWater(World worldIn, BlockPos pos) {
        boolean foundWater = false;

        for (Direction enumFacing : Direction.values()) {
            if (enumFacing != Direction.DOWN && worldIn.getFluidState(pos.offset(enumFacing)).isTagged(FluidTags.WATER)) {
                foundWater = true;
                break;
            }
        }

        if (foundWater) {
            if (worldIn.getEntitiesWithinAABB(ConstructionBlockEntity.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).isEmpty()) {
                worldIn.addEntity(new ConstructionBlockEntity(worldIn, pos, true));
            }
        }

        return foundWater;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean __a) {
        tryTouchWater(world, pos);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean __a) {
        if (!this.tryTouchWater(worldIn, pos)) {
            super.onBlockAdded(state, worldIn, pos, oldState, __a);
        }
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

}

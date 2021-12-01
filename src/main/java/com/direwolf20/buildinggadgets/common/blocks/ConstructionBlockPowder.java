package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionBlockPowder extends FallingBlock {
    public ConstructionBlockPowder() {
        super(Block.Properties.of(Material.SAND).strength(0.5F));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(TooltipTranslation.CONSTRUCTIONBLOCKPOWDER_HELPTEXT.componentTranslation());
    }

    @Override
    public void onLand(Level worldIn, BlockPos pos, BlockState p_176502_3_, BlockState p_176502_4_, FallingBlockEntity p_176502_5_) {
        if (worldIn.getFluidState(pos).is(FluidTags.WATER))
            worldIn.addFreshEntity(new ConstructionBlockEntity(worldIn, pos, true));
    }

    private boolean tryTouchWater(Level worldIn, BlockPos pos) {
        boolean foundWater = false;

        for (Direction enumFacing : Direction.values()) {
            if (enumFacing != Direction.DOWN && worldIn.getFluidState(pos.relative(enumFacing)).is(FluidTags.WATER)) {
                foundWater = true;
                break;
            }
        }

        if (foundWater) {
            if (worldIn.getEntitiesOfClass(ConstructionBlockEntity.class, new AABB(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).isEmpty()) {
                worldIn.addFreshEntity(new ConstructionBlockEntity(worldIn, pos, true));
            }
        }

        return foundWater;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean __a) {
        tryTouchWater(world, pos);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean __a) {
        if (!this.tryTouchWater(worldIn, pos)) {
            super.onPlace(state, worldIn, pos, oldState, __a);
        }
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

}

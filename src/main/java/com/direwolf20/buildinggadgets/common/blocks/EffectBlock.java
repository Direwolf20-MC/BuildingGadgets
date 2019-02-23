package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.utils.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class EffectBlock extends Block {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.MODID,"effect_block");
    public EffectBlock(Properties builder) {
        super(builder);
    }

    // @fixme: reimplement @since 1.13.x
//    public void initModel() {
//        //ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

    /**
     * @param state blockState
     * @return Render Type
     *
     * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
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

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * This gets a complete list of items dropped from this block.
     *
     * @param state   Current state
     * @param drops   add all items this block drops to this drops list
     * @param world   The current world, Currently hard 'World' and not 'IBlockReder' because vanilla needs it.
     * @param pos     Block position in world
     * @param fortune Breakers fortune level
     */
    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        drops.clear();
    }

    /**
     * @param state
     * @deprecated call via {@link IBlockState#getPushReaction()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }
}

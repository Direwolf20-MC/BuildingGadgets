package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.tiles.EffectBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EffectBlock extends Block {

    public static void spawnEffectBlock(World world, BlockPos spawnPos, BlockData spawnBlock, EffectBlockTileEntity.Mode mode, boolean usePaste) {
        BlockState state = BGBlocks.effectBlock.getDefaultState();
        world.setBlockState(spawnPos, state);
        ((EffectBlockTileEntity) world.getTileEntity(spawnPos)).initializeData(world, spawnBlock, mode, usePaste);
        // Send data to client
        world.notifyBlockUpdate(spawnPos, state, state, Constants.BlockFlags.DEFAULT);
    }

    public EffectBlock(Properties builder) {
        super(builder);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EffectBlockTileEntity();
    }

    // We still make effect blocks invisible because all effects (scaling block, transparent box) are dynamic so they has to be in the TER
    /**
     * @param state blockState
     * @return Render Type
     *
     * @deprecated call via {@link BlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
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
    /*@Override
    public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }*/

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
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder loot) {
        return new ArrayList<>();
    }

    /**
     * @param state
     * @deprecated call via {@link BlockState#getPushReaction()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}

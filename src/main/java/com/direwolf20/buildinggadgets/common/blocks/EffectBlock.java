package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tiles.EffectBlockTileEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EffectBlock extends Block {

    public enum Mode {
        // Serialization and networking based on `ordinal()`, please DO NOT CHANGE THE ORDER of the enums
        PLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                World world = builder.getWorld();
                BlockPos targetPos = builder.getPos();
                BlockData targetBlock = builder.getRenderedBlock();
                if (builder.isUsingPaste()) {
                    world.setBlockState(targetPos, OurBlocks.constructionBlock.getDefaultState());
                    TileEntity te = world.getTileEntity(targetPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(targetBlock, targetBlock);
                    }
                    world.addEntity(new ConstructionBlockEntity(world, targetPos, false));
                } else {
                    world.removeBlock(targetPos, false);

                    if( targetBlock.getState().getBlock() instanceof LeavesBlock)
                        targetBlock = new BlockData(targetBlock.getState().with(LeavesBlock.PERSISTENT, true), targetBlock.getTileData());

                    targetBlock.placeIn(SimpleBuildContext.builder().build(world), targetPos);
                    BlockPos upPos = targetPos.up();
                    world.getBlockState(targetPos).neighborChanged(world, targetPos, world.getBlockState(upPos).getBlock(), upPos, false);
                }
            }
        },
        REMOVE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                builder.getWorld().removeBlock(builder.getPos(), false);
            }
        },
        REPLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                spawnEffectBlock(builder.getWorld(), builder.getPos(), builder.getSourceBlock(), PLACE, builder.isUsingPaste());
            }
        };

        public static final Mode[] VALUES = values();

        public abstract void onBuilderRemoved(EffectBlockTileEntity builder);
    }

    public static void spawnUndoBlock(IBuildContext context, PlacementTarget target) {
        BlockState state = context.getWorld().getBlockState(target.getPos());

        TileEntity curTe = context.getWorld().getTileEntity(target.getPos());
        //can't use .isAir, because it's not build yet
        if (target.getData().getState() != Blocks.AIR.getDefaultState()) {
            Mode mode = state.isAir(context.getWorld(), target.getPos()) ? Mode.PLACE : Mode.REPLACE;
            spawnEffectBlock(curTe, state, context.getWorld(), target.getPos(), target.getData(), mode, false);
        } else if (! state.isAir(context.getWorld(), target.getPos())) {
            spawnEffectBlock(curTe, state, context.getWorld(), target.getPos(), TileSupport.createBlockData(state, curTe), Mode.REMOVE, false);
        }
    }

    public static void spawnEffectBlock(IBuildContext context, PlacementTarget target, Mode mode, boolean usePaste) {//TODO pass the buildcontext through, aka invert the overloading
        spawnEffectBlock(context.getWorld(), target.getPos(), target.getData(), mode, usePaste);
    }

    public static void spawnEffectBlock(IWorld world, BlockPos spawnPos, BlockData spawnBlock, Mode mode, boolean usePaste) {
        BlockState state = world.getBlockState(spawnPos);
        TileEntity curTe = world.getTileEntity(spawnPos);
        spawnEffectBlock(curTe, state, world, spawnPos, spawnBlock, mode, usePaste);
    }

    private static void spawnEffectBlock(@Nullable TileEntity curTe, BlockState curState, IWorld world, BlockPos spawnPos, BlockData spawnBlock, Mode mode, boolean usePaste) {
        BlockState state = OurBlocks.effectBlock.getDefaultState();
        world.setBlockState(spawnPos, state, 3);
        assert world.getTileEntity(spawnPos) != null;
        ((EffectBlockTileEntity) world.getTileEntity(spawnPos)).initializeData(curState, curTe, spawnBlock, mode, usePaste);
        // Send data to client
        if (world instanceof World)
            ((World) world).notifyBlockUpdate(spawnPos, state, state, Constants.BlockFlags.DEFAULT);
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

    /**
     * @param state blockState
     * @return Render Type
     * @deprecated call via {@link BlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        // We still make effect blocks invisible because all effects (scaling block, transparent box) are dynamic so they has to be in the TER
        return BlockRenderType.INVISIBLE;
    }

// 1.14
//    /**
//     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
//     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
//     */
//    @Override
//    public BlockRenderLayer getRenderLayer() {
//        // Since the effect block has no model rendering at all, which means we don't need blending, simply cutout is fine
//        return BlockRenderLayer.CUTOUT;
//    }

    /**
     * This gets a complete list of items dropped from this block.
     *
     * @param state Current state
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder loot) {
        return new ArrayList<>();
    }

    /**
     * @deprecated call via {@link BlockState#getPushReaction()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}

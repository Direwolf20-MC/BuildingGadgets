package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tileentities.EffectBlockTileEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                World world = builder.getLevel();
                if( world == null )
                    return;

                BlockPos targetPos = builder.getBlockPos();
                BlockData targetBlock = builder.getRenderedBlock();
                if (builder.isUsingPaste()) {
                    world.setBlockAndUpdate(targetPos, OurBlocks.CONSTRUCTION_BLOCK.get().defaultBlockState());
                    TileEntity te = world.getBlockEntity(targetPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(targetBlock);
                    }
                    world.addFreshEntity(new ConstructionBlockEntity(world, targetPos, false));
                } else {
                    if( targetBlock.getState().getBlock() instanceof LeavesBlock) {
                        targetBlock = new BlockData(targetBlock.getState().setValue(LeavesBlock.PERSISTENT, true), targetBlock.getTileData());
                    }

                    targetBlock.placeIn(BuildContext.builder().build(world), targetPos);

                    // Instead of removing the block, we just sync the client & server to know that the block has been replaced
                    world.sendBlockUpdated(targetPos, targetBlock.getState(), targetBlock.getState(), Constants.BlockFlags.DEFAULT);

                    BlockPos upPos = targetPos.above();
                    world.getBlockState(targetPos).neighborChanged(world, targetPos, world.getBlockState(upPos).getBlock(), upPos, false);
                }
            }
        },
        REMOVE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                builder.getLevel().removeBlock(builder.getBlockPos(), false);
            }
        },
        REPLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                spawnEffectBlock(builder.getLevel(), builder.getBlockPos(), builder.getSourceBlock(), PLACE, builder.isUsingPaste());
            }
        };

        public static final Mode[] VALUES = values();

        public abstract void onBuilderRemoved(EffectBlockTileEntity builder);
    }

    /**
     * As the effect block is effectively air it needs to have a material just like Air.
     * We don't use Material.AIR as this is replaceable.
     */
    private static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.NONE).nonSolid().build();

    public static void spawnUndoBlock(BuildContext context, PlacementTarget target) {
        BlockState state = context.getWorld().getBlockState(target.getPos());

        TileEntity curTe = context.getWorld().getBlockEntity(target.getPos());
        //can't use .isAir, because it's not build yet
        if (target.getData().getState() != Blocks.AIR.defaultBlockState()) {
            Mode mode = state.isAir(context.getWorld(), target.getPos()) ? Mode.PLACE : Mode.REPLACE;
            spawnEffectBlock(curTe, state, context.getWorld(), target.getPos(), target.getData(), mode, false);
        } else if (! state.isAir(context.getWorld(), target.getPos())) {
            spawnEffectBlock(curTe, state, context.getWorld(), target.getPos(), TileSupport.createBlockData(state, curTe), Mode.REMOVE, false);
        }
    }

    public static void spawnEffectBlock(BuildContext context, PlacementTarget target, Mode mode, boolean usePaste) {//TODO pass the buildcontext through, aka invert the overloading
        spawnEffectBlock(context.getWorld(), target.getPos(), target.getData(), mode, usePaste);
    }

    public static void spawnEffectBlock(IWorld world, BlockPos spawnPos, BlockData spawnBlock, Mode mode, boolean usePaste) {
        BlockState state = world.getBlockState(spawnPos);
        TileEntity curTe = world.getBlockEntity(spawnPos);
        spawnEffectBlock(curTe, state, world, spawnPos, spawnBlock, mode, usePaste);
    }

    private static void spawnEffectBlock(@Nullable TileEntity curTe, BlockState curState, IWorld world, BlockPos spawnPos, BlockData spawnBlock, Mode mode, boolean usePaste) {
        BlockState state = OurBlocks.EFFECT_BLOCK.get().defaultBlockState();
        world.setBlock(spawnPos, state, 3);

        TileEntity tile = world.getBlockEntity(spawnPos);
        if (!(tile instanceof EffectBlockTileEntity)) {
            // Fail safely by replacing with air. Kinda voids but meh...
            world.setBlock(spawnPos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        ((EffectBlockTileEntity) tile).initializeData(curState, curTe, spawnBlock, mode, usePaste);
        // Send data to client
        if (world instanceof World)
            ((World) world).sendBlockUpdated(spawnPos, state, state, Constants.BlockFlags.DEFAULT);
    }

    public EffectBlock() {
        super(Block.Properties.of(EFFECT_BLOCK_MATERIAL)
                .strength(20f)
                .noDrops());
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

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.block();
    }

    /**
     * @param state blockState
     * @return Render Type
     * @deprecated call via {@link BlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderShape(BlockState state) {
        // We still make effect blocks invisible because all effects (scaling block, transparent box) are dynamic so they has to be in the TER
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean skipRendering(BlockState p_200122_1_, BlockState p_200122_2_, Direction p_200122_3_) {
        return true;
    }

    /**
     * This gets a complete list of items dropped from this block.
     *
     * @param p_220076_1_ Current state
     */
    @Override
    public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
        return new ArrayList<>();
    }

    /**
     * @deprecated call via {@link BlockState#getPushReaction()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1.0f;
    }
}

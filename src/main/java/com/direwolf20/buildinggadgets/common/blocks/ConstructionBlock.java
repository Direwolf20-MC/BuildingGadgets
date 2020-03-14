package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

//@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends Block /*implements IFacade*/ {
    //TODO Review which methods we are overriding -- a LOT more were added at some point, many of which I probably don't want in here :).
    public static final IProperty<Boolean> BRIGHT = BooleanProperty.create("bright");
    public static final IProperty<Boolean> NEIGHBOR_BRIGHTNESS = BooleanProperty.create("neighbor_brightness");
    public static final IProperty<Boolean> AMBIENT_OCCLUSION = BooleanProperty.create("ambient_occlusion");

    public ConstructionBlock(Properties builder) {
        super(builder);
        setDefaultState(this.getStateContainer().getBaseState().with(BRIGHT, true).with(NEIGHBOR_BRIGHTNESS, false).with(AMBIENT_OCCLUSION, false));
    }

    @Override
    public boolean isVariableOpacity() {
        return true;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BRIGHT, NEIGHBOR_BRIGHTNESS, AMBIENT_OCCLUSION);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ConstructionBlockTileEntity.TYPE.create();
    }

    public boolean isMimicNull(BlockState mimicBlock) {
        return (mimicBlock == null || mimicBlock == Blocks.AIR.getDefaultState());
    }

    @Nullable
    private BlockState getActualMimicBlock(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            return ((ConstructionBlockTileEntity) te).getActualBlockData().getState();
        }
        return null;
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Boolean bright = state.get(ConstructionBlock.BRIGHT);
        if (bright) {
            return 0;
        }
        return 255;
    }

// 1.14 code
//    @Override
//    public boolean doesSideBlockRendering(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
//        BlockState mimic = getActualMimicBlock(world, pos);
//        return !isMimicNull(mimic) ? mimic.doesSideBlockRendering(world, pos, face) : super.doesSideBlockRendering(state, world, pos, face);
//    }

    public void initColorHandler(BlockColors blockColors) {
        blockColors.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                BlockState mimicBlock = getActualMimicBlock(world, pos);
                try {
                    return blockColors.getColor(mimicBlock, world, pos, tintIndex);
                } catch (Exception var8) {
                    return - 1;
                }
            }
            return -1;
        }, this);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext selectionContext) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getCollisionShape(worldIn, pos) : super.getCollisionShape(state, worldIn, pos, selectionContext);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.onEntityCollision(worldIn, pos, entityIn);
        } else {
            super.onEntityCollision(state, worldIn, pos, entityIn);
        }
    }

    /**
     * For all neighbors, have them react to this block's existence, potentially updating their states as needed. For
     * example, fences make their connections to this block if possible and observers pulse if this block was placed in
     * front of their detector
     */
    @Override
    public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.updateNeighbors(worldIn, pos, flags);
        } else {
            super.updateNeighbors(stateIn, worldIn, pos, flags);
        }
    }

    /**
     * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
     * from the docs for {@link BlockState#updateDiagonalNeighbors(IWorld, BlockPos, int)}.
     */
    @Override
    public void updateDiagonalNeighbors(BlockState state, IWorld worldIn, BlockPos pos, int flags) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.updateDiagonalNeighbors(worldIn, pos, flags);
        } else {
            super.updateDiagonalNeighbors(state, worldIn, pos, flags);
        }
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {

        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.allowsMovement(worldIn, pos, type) : super.allowsMovement(state, worldIn, pos, type);
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return super.isSideInvisible(state, adjacentBlockState, side); //TODO Find a way to make this work, glass adjacent to fake glass shows the middle side.
        /*boolean bright = state.get(ConstructionBlock.BRIGHT);
        if (!bright) return false;
        if (adjacentBlockState.getBlock() instanceof ConstructionBlock) {
            return !adjacentBlockState.get(ConstructionBlock.BRIGHT);
        } else {
            //This is how vanilla BreakableBlock does it.
            return adjacentBlockState.getBlock() == this ? true : super.isSideInvisible(state, adjacentBlockState, side);
        }*/
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext selectionContext) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getShape(worldIn, pos) : super.getShape(state, worldIn, pos, selectionContext);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!mimic.isSolid()) {
            return VoxelShapes.empty();
        }
        return !isMimicNull(mimic) ? mimic.getRenderShape(worldIn, pos) : super.getRenderShape(state, worldIn, pos);
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getRaytraceShape(worldIn, pos) : super.getRaytraceShape(state, worldIn, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(reader, pos);
        return !isMimicNull(mimic) ? mimic.propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
    }

    /*@Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState mimic = getActualMimicBlock(world, pos);
        if (!isMimicNull(mimic)) {
            mimic.randomTick(world, pos, random);
        } else {
            super.randomTick(state, world, pos, random);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.getBlock().tick(state, worldIn, pos, random);
        } else {
            super.tick(state, worldIn, pos, random);
        }
    }*/

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to ? and ?, and will always be called regardless
     * of whether the block can receive random update ticks
     */
    /*@Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            try {
                mimic.getBlock().animateTick(stateIn, worldIn, pos, rand);
            } catch (Exception var8) {
                super.animateTick(stateIn, worldIn, pos, rand);
            }
        } else {
            super.animateTick(stateIn, worldIn, pos, rand);
        }
    }*/

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.neighborChanged(worldIn, pos, blockIn, fromPos, p_220069_6_);
        } else {
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos, p_220069_6_);
        }
    }

    /**
     * Get the hardness of this Block relative to the ability of the given player
     * @deprecated call via {@link BlockState#getPlayerRelativeBlockHardness(PlayerEntity, IBlockReader, BlockPos)} whenever
     * possible. Implementing/overriding is fine.
     */
    /*@Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getPlayerRelativeBlockHardness(player, worldIn, pos) : super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return ! isMimicNull(mimic) ? super.onBlockActivated(state, worldIn, pos, player, handIn, hit) : super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }*/

    /**
     * Called when the given entity walks on this Block
     */
    /*@Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.getBlock().onEntityWalk(worldIn, pos, entityIn);
        } else {
            super.onEntityWalk(worldIn, pos, entityIn);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            super.onBlockClicked(state, worldIn, pos, player);
            //mimic.onBlockClicked(worldIn, pos, player);
        } else {
            super.onBlockClicked(state, worldIn, pos, player);
        }
    }*/

    /**
     * @deprecated call via {@link BlockState#getWeakPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    /*@Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        BlockState mimic = getActualMimicBlock(blockAccess, pos);
        return !isMimicNull(mimic) ? mimic.getWeakPower(blockAccess, pos, side) : super.getWeakPower(blockState, blockAccess, pos, side);
    }*/

    /**
     * @deprecated call via {@link BlockState#getStrongPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    /*@Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        BlockState mimic = getActualMimicBlock(blockAccess, pos);
        return !isMimicNull(mimic) ? mimic.getStrongPower(blockAccess, pos, side) : super.getStrongPower(blockState, blockAccess, pos, side);
    }*/

    /**
     * @deprecated call via {@link BlockState#getPushReaction()} whenever possible. Implementing/overriding is fine.
     */
    /*@Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }*/

    /**
     * Block's chance to react to a living entity falling on it.
     */
    /*@Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.getBlock().onFallenUpon(worldIn, pos, entityIn, fallDistance);
        } else {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
    }*/

    /**
     * Called similar to random ticks, but only when it is raining.
     */
    /*@Override
    public void fillWithRain(World worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.getBlock().fillWithRain(worldIn, pos);
        } else {
            super.fillWithRain(worldIn, pos);
        }
    }*/

    /**
     * @deprecated call via whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public Vec3d getOffset(BlockState state, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getOffset(worldIn, pos) : super.getOffset(state, worldIn, pos);
    }

    /*@Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable) {
        BlockState mimic = getActualMimicBlock(world, pos);
        return !isMimicNull(mimic) ? mimic.canSustainPlant(world, pos, facing, plantable) : super.canSustainPlant(state, world, pos, facing, plantable);
    }*/

    // Todo re-eval
   /* @Override
    @OnlyIn(Dist.CLIENT)
    public static boolean shouldSideBeRendered(BlockState adjacentState, IBlockReader blockState, BlockPos blockAccess, Direction pos) {
        //FakeRenderWorld fakeWorld = new FakeRenderWorld();

        BlockState mimicBlock = getActualMimicBlock(blockState, blockAccess);
        if (mimicBlock == null) {
            return true;
        }
        BlockState sideBlockState = blockState.getBlockState(blockAccess.offset(pos));
        if (sideBlockState.getBlock().equals(OurBlocks.constructionBlock)) {
            if (!(getActualMimicBlock(blockState, blockAccess.offset(pos)) == null)) {
                sideBlockState = getActualMimicBlock(blockState, blockAccess.offset(pos));
            }
        }

        try {
            return mimicBlock.getBlock().shouldSideBeRendered(sideBlockState, blockState, blockAccess, pos);
        } catch (Exception var8) {
            return true;
        }
    }*/


    @Override
    public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
        BlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock == null) {
            return super.isNormalCube(state, world, pos);
        }
        try {
            return mimicBlock.getBlock().isNormalCube(mimicBlock, world, pos);
        } catch (Exception var8) {
            return super.isNormalCube(state, world, pos);
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Boolean bright = state.get(ConstructionBlock.BRIGHT);
        Boolean neighborBrightness = state.get(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
        if (bright || neighborBrightness) {
            return 1f;
        }
        return 0.2f;
    }
}
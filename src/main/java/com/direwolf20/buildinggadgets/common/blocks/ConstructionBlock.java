package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Random;

//@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends Block /*implements IFacade*/ {
    //TODO Review which methods we are overriding -- a LOT more were added at some point, many of which I probably don't want in here :).
    public static final Property<Boolean> BRIGHT = BooleanProperty.create("bright");
    public static final Property<Boolean> NEIGHBOR_BRIGHTNESS = BooleanProperty.create("neighbor_brightness");
    public static final Property<Boolean> AMBIENT_OCCLUSION = BooleanProperty.create("ambient_occlusion");

    public ConstructionBlock() {
        super(Block.Properties.create(Material.SAND).hardnessAndResistance(2f, 0f).harvestTool(ToolType.SHOVEL));
        setDefaultState(this.getStateContainer().getBaseState().with(BRIGHT, true).with(NEIGHBOR_BRIGHTNESS, false).with(AMBIENT_OCCLUSION, false));
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof ConstructionBlockTileEntity)) {
            return super.addDestroyEffects(state, world, pos, manager);
        }

        manager.addBlockDestroyEffects(pos, tileEntity.getBlockState());
        return true;
    }

    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        if (target.getType() != RayTraceResult.Type.BLOCK) {
            return super.addHitEffects(state, world, target, manager);
        }

        BlockPos pos = ((BlockRayTraceResult) target).getPos();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof ConstructionBlockTileEntity)) {
            return super.addHitEffects(state, world, target, manager);
        }

        this.addBlockHitEffects(world, pos, tileEntity.getBlockState(), ((BlockRayTraceResult) target).getFace(), manager);
        return true;
    }

    /**
     * Stolen from the particle manager to handle the custom state (minified code a bit)
     */
    public void addBlockHitEffects(World world, BlockPos pos, BlockState blockstate, Direction side, ParticleManager manager) {
        if (blockstate.getRenderType() == BlockRenderType.INVISIBLE) return;
        Random rand = new Random();
        int i = pos.getX(), j = pos.getY(), k = pos.getZ();
        AxisAlignedBB axisalignedbb = blockstate.getShape(world, pos).getBoundingBox();
        double x = (double)i + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - (double)0.2F) + (double)0.1F + axisalignedbb.minX;
        double y = (double)j + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - (double)0.2F) + (double)0.1F + axisalignedbb.minY;
        double z = (double)k + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - (double)0.2F) + (double)0.1F + axisalignedbb.minZ;
        if (side == Direction.DOWN || side == Direction.UP)
            y = side == Direction.DOWN ? (double)j + axisalignedbb.minY - (double)0.1F : (double)j + axisalignedbb.maxY + (double)0.1F;
        if (side == Direction.NORTH || side == Direction.SOUTH)
            z = side == Direction.NORTH ? (double)k + axisalignedbb.minZ - (double)0.1F : (double)k + axisalignedbb.maxZ + (double)0.1F;
        if (side == Direction.WEST || side == Direction.EAST)
            x = side == Direction.WEST ? (double)i + axisalignedbb.minX - (double)0.1F : (double)i + axisalignedbb.maxX + (double)0.1F;
        manager.addEffect((new DiggingParticle((ClientWorld) world, x, y, z, 0.0D, 0.0D, 0.0D, blockstate)).setBlockPos(pos).multiplyVelocity(0.2F).multiplyParticleScaleBy(0.6F));
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
        return OurTileEntities.CONSTRUCTION_BLOCK_TILE_ENTITY.get().create();
    }

    public boolean isMimicNull(BlockState mimicBlock) {
        return (mimicBlock == null || mimicBlock == Blocks.AIR.getDefaultState());
    }

    @Nullable
    private BlockState getActualMimicBlock(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            return ((ConstructionBlockTileEntity) te).getConstructionBlockData().getState();
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
// removed as of 1.16
// todo: eval replacement
//    @Override
//    public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
//        BlockState mimic = getActualMimicBlock(worldIn, pos);
//        if (!isMimicNull(mimic)) {
//            mimic.updateNeighbors(worldIn, pos, flags);
//        } else {
//            super.updateNeighbors(stateIn, worldIn, pos, flags);
//        }
//    }

    /**
     * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
     * from the docs for {@link BlockState#updateDiagonalNeighbors(IWorld, BlockPos, int)}.
     *
     * removed as of 1.16
     * todo: eval replacement
     */
//    @Override
//    public void updateDiagonalNeighbors(BlockState state, IWorld worldIn, BlockPos pos, int flags, int something) {
//        BlockState mimic = getActualMimicBlock(worldIn, pos);
//        if (!isMimicNull(mimic)) {
//            mimic.updateDiagonalNeighbors(worldIn, pos, flags);
//        } else {
//            super.updateDiagonalNeighbors(state, worldIn, pos, flags, something);
//        }
//    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {

        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.allowsMovement(worldIn, pos, type) : super.allowsMovement(state, worldIn, pos, type);
    }

// @todo: removed 1.16, find replacement
//    @Override
//    public boolean hasTileEntity() {
//        return true;
//    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (state.getBlock().equals(adjacentBlockState.getBlock())) return false;
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
        // dummy is likely wrong.
        return !isMimicNull(mimic) ? mimic.getRaytraceShape(worldIn, pos, ISelectionContext.dummy()) : super.getRaytraceShape(state, worldIn, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(reader, pos);
        return !isMimicNull(mimic) ? mimic.propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
    }

    /**
     * @deprecated call via whenever possible.
     * Implementing/overriding is fine.
     *
     * todo: removed 1.16 find replacement
     */
//    @Override
//    public Vector3d getOffset(BlockState state, IBlockReader worldIn, BlockPos pos) {
//        BlockState mimic = getActualMimicBlock(worldIn, pos);
//        return !isMimicNull(mimic) ? mimic.getOffset(worldIn, pos) : super.getOffset(state, worldIn, pos);
//    }

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


// todo: removed 1.16 - find replacement
//    @Override
//    public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
//        BlockState mimicBlock = getActualMimicBlock(world, pos);
//        if (mimicBlock == null) {
//            return super.isNormalCube(state, world, pos);
//        }
//        try {
//            return mimicBlock.getBlock().isNormalCube(mimicBlock, world, pos);
//        } catch (Exception var8) {
//            return super.isNormalCube(state, world, pos);
//        }
//    }

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

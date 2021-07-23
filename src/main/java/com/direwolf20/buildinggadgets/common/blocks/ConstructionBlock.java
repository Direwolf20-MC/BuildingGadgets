package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tileentities.OurTileEntities;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

//@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends Block implements EntityBlock /*implements IFacade*/ {
    //TODO Review which methods we are overriding -- a LOT more were added at some point, many of which I probably don't want in here :).
    public static final Property<Boolean> BRIGHT = BooleanProperty.create("bright");
    public static final Property<Boolean> NEIGHBOR_BRIGHTNESS = BooleanProperty.create("neighbor_brightness");
    public static final Property<Boolean> AMBIENT_OCCLUSION = BooleanProperty.create("ambient_occlusion");

    public ConstructionBlock() {
        super(Block.Properties.of(Material.SAND).strength(1.5F, 6.0F));
        registerDefaultState(this.getStateDefinition().any().setValue(BRIGHT, true).setValue(NEIGHBOR_BRIGHTNESS, false).setValue(AMBIENT_OCCLUSION, false));
    }

//    @Override
//    public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
//        BlockEntity tileEntity = world.getBlockEntity(pos);
//        if (!(tileEntity instanceof ConstructionBlockTileEntity)) {
//            return super.addDestroyEffects(state, world, pos, manager);
//        }
//
//        manager.destroy(pos, tileEntity.getBlockState());
//        return true;
//    }
//
//    @Override
//    public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
//        if (target.getType() != HitResult.Type.BLOCK) {
//            return super.addHitEffects(state, world, target, manager);
//        }
//
//        BlockPos pos = ((BlockHitResult) target).getBlockPos();
//        BlockEntity tileEntity = world.getBlockEntity(pos);
//        if (!(tileEntity instanceof ConstructionBlockTileEntity)) {
//            return super.addHitEffects(state, world, target, manager);
//        }
//
//        this.addBlockHitEffects(world, pos, tileEntity.getBlockState(), ((BlockHitResult) target).getDirection(), manager);
//        return true;
//    }

//    /**
//     * Stolen from the particle manager to handle the custom state (minified code a bit)
//     */
//    public void addBlockHitEffects(Level world, BlockPos pos, BlockState blockstate, Direction side, ParticleEngine manager) {
//        if (blockstate.getRenderShape() == RenderShape.INVISIBLE) return;
//        Random rand = new Random();
//        int i = pos.getX(), j = pos.getY(), k = pos.getZ();
//        AABB axisalignedbb = blockstate.getShape(world, pos).bounds();
//        double x = (double)i + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - (double)0.2F) + (double)0.1F + axisalignedbb.minX;
//        double y = (double)j + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - (double)0.2F) + (double)0.1F + axisalignedbb.minY;
//        double z = (double)k + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - (double)0.2F) + (double)0.1F + axisalignedbb.minZ;
//        if (side == Direction.DOWN || side == Direction.UP)
//            y = side == Direction.DOWN ? (double)j + axisalignedbb.minY - (double)0.1F : (double)j + axisalignedbb.maxY + (double)0.1F;
//        if (side == Direction.NORTH || side == Direction.SOUTH)
//            z = side == Direction.NORTH ? (double)k + axisalignedbb.minZ - (double)0.1F : (double)k + axisalignedbb.maxZ + (double)0.1F;
//        if (side == Direction.WEST || side == Direction.EAST)
//            x = side == Direction.WEST ? (double)i + axisalignedbb.minX - (double)0.1F : (double)i + axisalignedbb.maxX + (double)0.1F;
//        manager.add((new TerrainParticle((ClientLevel) world, x, y, z, 0.0D, 0.0D, 0.0D, blockstate)).init(pos).setPower(0.2F).scale(0.6F));
//    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BRIGHT, NEIGHBOR_BRIGHTNESS, AMBIENT_OCCLUSION);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return OurTileEntities.CONSTRUCTION_BLOCK_TILE_ENTITY.get().create(blockPos, blockState);
    }

    public boolean isMimicNull(BlockState mimicBlock) {
        return (mimicBlock == null || mimicBlock == Blocks.AIR.defaultBlockState());
    }

    @Nullable
    private BlockState getActualMimicBlock(BlockGetter blockAccess, BlockPos pos) {
        BlockEntity te = blockAccess.getBlockEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            return ((ConstructionBlockTileEntity) te).getConstructionBlockData().getState();
        }
        return null;
    }

    @Override
    @Deprecated
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        Boolean bright = state.getValue(ConstructionBlock.BRIGHT);
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext selectionContext) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getCollisionShape(worldIn, pos) : super.getCollisionShape(state, worldIn, pos, selectionContext);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.entityInside(worldIn, pos, entityIn);
        } else {
            super.entityInside(state, worldIn, pos, entityIn);
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {

        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.isPathfindable(worldIn, pos, type) : super.isPathfindable(state, worldIn, pos, type);
    }

// @todo: removed 1.16, find replacement
//    @Override
//    public boolean hasTileEntity() {
//        return true;
//    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (state.getBlock().equals(adjacentBlockState.getBlock())) return false;
        return super.skipRendering(state, adjacentBlockState, side); //TODO Find a way to make this work, glass adjacent to fake glass shows the middle side.
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext selectionContext) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getShape(worldIn, pos) : super.getShape(state, worldIn, pos, selectionContext);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!mimic.canOcclude()) {
            return Shapes.empty();
        }
        return !isMimicNull(mimic) ? mimic.getBlockSupportShape(worldIn, pos) : super.getOcclusionShape(state, worldIn, pos);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        // dummy is likely wrong.
        return !isMimicNull(mimic) ? mimic.getVisualShape(worldIn, pos, CollisionContext.empty()) : super.getInteractionShape(state, worldIn, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
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
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        Boolean bright = state.getValue(ConstructionBlock.BRIGHT);
        Boolean neighborBrightness = state.getValue(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
        if (bright || neighborBrightness) {
            return 1f;
        }
        return 0.2f;
    }
}

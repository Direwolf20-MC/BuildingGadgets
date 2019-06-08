package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends ContainerBlock /*implements IFacade*/ {
    //public static final ConstructionProperty FACADEID = new ConstructionProperty("facadeid");
    public static final IProperty<Boolean> BRIGHT = BooleanProperty.create("bright");
    public static final IProperty<Boolean> NEIGHBOR_BRIGHTNESS = BooleanProperty.create("neighbor_brightness");

    /* TODO revise when connected Textures will be supported again - supporting BlockState's as Properties is impossible
    public static final IUnlistedProperty<BlockState> FACADE_ID = new BlockStateProperty("facadestate");
    public static final IUnlistedProperty<BlockState> FACADE_EXT_STATE = new BlockStateProperty("facadeextstate");
*/
    public ConstructionBlock(Properties builder) {
        super(builder);

        setDefaultState(this.getStateContainer().getBaseState().with(BRIGHT, true).with(NEIGHBOR_BRIGHTNESS, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BRIGHT, NEIGHBOR_BRIGHTNESS);
    }

    /*
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(BlockState iBlockState) {
                return ConstructionBakedModel.modelFacade;
            }
        };
        ModelLoader.setCustomStateMapper(this, ignoreState);
    }*/

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return new ArrayList<ItemStack>(){{
            add(new ItemStack(BGItems.constructionPaste));
        }};
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new ConstructionBlockTileEntity();
    }

    public boolean isMimicNull(BlockState mimicBlock) {
        return (mimicBlock == null || mimicBlock == Blocks.AIR.getDefaultState());
    }
    /*private static ConstructionBlockTileEntity getTE(World world, BlockPos pos) {
        return (ConstructionBlockTileEntity) world.getTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, ClientPlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        //super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        ConstructionBlockTileEntity te = getTE(world, pos);
        ItemStack heldItem = player.getHeldItem(hand);
        BlockState newState = Block.getBlockFromItem(heldItem.getItem()).getStateFromMeta(heldItem.getMetadata());
        if (newState != null && newState != Blocks.AIR.getDefaultState()) {
            te.setBlockState(newState);
            te.setActualBlockState(newState);
            return true;
        }
        System.out.println("Failed: " + newState + ":" + te.getBlockState() + ":" + world.isRemote + ":" + te.getActualBlockState());
        return false;
    }*/

    /**
     * Can return IExtendedBlockState
     */
    /*@Override
    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        BlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock != null) {
            FakeRenderWorld fakeRenderWorld = new FakeRenderWorld();
            fakeRenderWorld.setState((World) world, mimicBlock, pos);
            BlockState extState = mimicBlock.getBlock().getExtendedState(mimicBlock, fakeRenderWorld, pos);
            //ConstructionID mimicID = new ConstructionID(mimicBlock);
            return extendedBlockState.withProperty(FACADE_ID, mimicBlock).withProperty(FACADE_EXT_STATE, extState);
        }
        return extendedBlockState;
    }*/

    @Nullable
    private BlockState getActualMimicBlock(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            return ((ConstructionBlockTileEntity) te).getActualBlockState();
        }
        return null;
    }

    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return true; // delegated to FacadeBakedModel#getQuads
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        BlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, blockAccess, pos, side);
    }*/

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Boolean bright = state.get(ConstructionBlock.BRIGHT);
        if (bright) {
            return 0;
        }
        return 255;
        //BlockState mimic = getActualMimicBlock(worldIn, pos);
        //return mimic != null ? mimic.getOpacity(worldIn, pos) : super.getOpacity(state, worldIn, pos);
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, IEnviromentBlockReader world, BlockPos pos, Direction face) {
        BlockState mimic = getActualMimicBlock(world, pos);
        return !isMimicNull(mimic) ? mimic.doesSideBlockRendering(world, pos, face) : super.doesSideBlockRendering(state, world, pos, face);
    }

    public void initColorHandler(BlockColors blockColors) {
        blockColors.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                BlockState mimicBlock = getActualMimicBlock(world, pos);
                return blockColors.getColor(mimicBlock, (World) world, pos);
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

    /**
     * @deprecated call via {@link BlockState#getBlockHardness(IBlockReader, BlockPos)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getBlockHardness(worldIn, pos) : super.getBlockHardness(blockState, worldIn, pos);
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext selectionContext) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getShape(worldIn, pos) : super.getShape(state, worldIn, pos, selectionContext);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
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

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.randomTick(worldIn, pos, random);
        } else {
            super.randomTick(state, worldIn, pos, random);
        }
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.randomTick(worldIn, pos, random);
        } else {
            super.tick(state, worldIn, pos, random);
        }
    }

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to {@link } and {@link #?}, and will always be called regardless
     * of whether the block can receive random update ticks
     */
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.randomTick(worldIn, pos, rand);
        } else {
            super.animateTick(stateIn, worldIn, pos, rand);
        }
    }

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
    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getPlayerRelativeBlockHardness(player, worldIn, pos) : super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        //System.out.println(mimic);
        return !isMimicNull(mimic) ? mimic.onBlockActivated(worldIn, player, hand, rayTraceResult) : super.onBlockActivated(state, worldIn, pos, player, hand, rayTraceResult);
    }

    /**
     * Called when the given entity walks on this Block
     */
    @Override
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
            mimic.onBlockClicked(worldIn, pos, player);
        } else {
            super.onBlockClicked(state, worldIn, pos, player);
        }
    }

    /**
     * @deprecated call via {@link BlockState#getWeakPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        BlockState mimic = getActualMimicBlock(blockAccess, pos);
        return !isMimicNull(mimic) ? mimic.getWeakPower(blockAccess, pos, side) : super.getWeakPower(blockState, blockAccess, pos, side);
    }

    /**
     * @deprecated call via {@link BlockState#getStrongPower(IBlockReader, BlockPos, Direction)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        BlockState mimic = getActualMimicBlock(blockAccess, pos);
        return !isMimicNull(mimic) ? mimic.getStrongPower(blockAccess, pos, side) : super.getStrongPower(blockState, blockAccess, pos, side);
    }

    /**
     * @deprecated call via {@link BlockState#getPushReaction()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    /**
     * Block's chance to react to a living entity falling on it.
     */
    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.getBlock().onFallenUpon(worldIn, pos, entityIn, fallDistance);
        } else {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
    }

    /**
     * Called similar to random ticks, but only when it is raining.
     */
    @Override
    public void fillWithRain(World worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        if (!isMimicNull(mimic)) {
            mimic.getBlock().fillWithRain(worldIn, pos);
        } else {
            super.fillWithRain(worldIn, pos);
        }
    }

    /**
     * @deprecated call via whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public Vec3d getOffset(BlockState state, IBlockReader worldIn, BlockPos pos) {
        BlockState mimic = getActualMimicBlock(worldIn, pos);
        return !isMimicNull(mimic) ? mimic.getOffset(worldIn, pos) : super.getOffset(state, worldIn, pos);
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable) {
        BlockState mimic = getActualMimicBlock(world, pos);
        return !isMimicNull(mimic) ? mimic.canSustainPlant(world, pos, facing, plantable) : super.canSustainPlant(state, world, pos, facing, plantable);
    }

 /* Todo reeval
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        FakeRenderWorld fakeWorld = new FakeRenderWorld();

        BlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        if (mimicBlock == null) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
        BlockState sideBlockState = blockAccess.getBlockState(pos.offset(side));
        if (sideBlockState.getBlock().equals(ModBlocks.constructionBlock)) {
            if (!(getActualMimicBlock(blockAccess, pos.offset(side)) == null)) {
                sideBlockState = getActualMimicBlock(blockAccess, pos.offset(side));
            }
        }

        fakeWorld.setState(blockAccess, mimicBlock, pos);
        fakeWorld.setState(blockAccess, sideBlockState, pos.offset(side));

        try {
            return mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, fakeWorld, pos, side);
        } catch (Exception var8) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }*/

 /*
    @Override
    public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos) {
        BlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock == null) {
            return super.isNormalCube(state, world, pos);
        }
        try {
            return mimicBlock.getBlock().isNormalCube(mimicBlock, world, pos);
        } catch (Exception var8) {
            return super.isNormalCube(state, world, pos);
        }
    }*/

// fixme: removed as of 1.14?
//    @Deprecated
//    public float getAmbientOcclusionLightValue(BlockState state) {
//        Boolean bright = state.get(ConstructionBlock.BRIGHT);
//        Boolean neighborBrightness = state.get(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
//        if (bright || neighborBrightness) {
//            return 1f;
//        }
//        return 0.2f;
//    }
}
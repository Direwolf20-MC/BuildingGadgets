package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingObjects;
import com.direwolf20.buildinggadgets.common.world.FakeRenderWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

//@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends Block /*implements IFacade*/ {

    //public static final ConstructionProperty FACADEID = new ConstructionProperty("facadeid");
    public static final IProperty<Boolean> BRIGHT = BooleanProperty.create("bright");
    public static final IProperty<Boolean> NEIGHBOR_BRIGHTNESS = BooleanProperty.create("neighbor_brightness");

    /* TODO revise when connected Textures will be supported again - supporting BlockState's as Properties is impossible
    public static final IUnlistedProperty<IBlockState> FACADE_ID = new BlockStateProperty("facadestate");
    public static final IUnlistedProperty<IBlockState> FACADE_EXT_STATE = new BlockStateProperty("facadeextstate");
*/
    public ConstructionBlock(Builder builder) {
        super(builder);

        setDefaultState(this.getStateContainer().getBaseState().with(BRIGHT, false).with(NEIGHBOR_BRIGHTNESS, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BRIGHT, NEIGHBOR_BRIGHTNESS);
    }

    /*
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return ConstructionBakedModel.modelFacade;
            }
        };
        ModelLoader.setCustomStateMapper(this, ignoreState);
    }*/

    @Override
    @Nonnull
    public IItemProvider getItemDropped(IBlockState state, World worldIn, BlockPos pos, int fortune) {
        return BuildingObjects.constructionPaste;
    }

    @Override
    public boolean canSilkHarvest(IBlockState state, IWorldReader world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    /*private static ConstructionBlockTileEntity getTE(World world, BlockPos pos) {
        return (ConstructionBlockTileEntity) world.getTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        //super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        ConstructionBlockTileEntity te = getTE(world, pos);
        ItemStack heldItem = player.getHeldItem(hand);
        IBlockState newState = Block.getBlockFromItem(heldItem.getItem()).getStateFromMeta(heldItem.getMetadata());
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
     *
     * @param state
     * @param world
     * @param pos
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockReader world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
            FakeRenderWorld fakeRenderWorld = new FakeRenderWorld();
            fakeRenderWorld.setState((World) world, mimicBlock, pos);
            IBlockState extState = mimicBlock.getBlock().getExtendedState(mimicBlock, fakeRenderWorld, pos);
// @todo: reimplement @since 1.13.x
//            ConstructionID mimicID = new ConstructionID(mimicBlock);
//            return extendedBlockState.withProperty(FACADE_ID, mimicBlock).withProperty(FACADE_EXT_STATE, extState);

        return extendedBlockState;
    }

    @Nullable
    private IBlockState getActualMimicBlock(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            return ((ConstructionBlockTileEntity) te).getActualBlockState();
        }
        return null;
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // delegated to FacadeBakedModel#getQuads
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, blockAccess, pos, side);
    }*/

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
        IBlockState mimic = getActualMimicBlock(world, pos);
        return mimic != null ? mimic.isNormalCube(world, pos) : super.isNormalCube(state, world, pos);
    }


    @Override
    public int getOpacity(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getOpacity(worldIn, pos) : super.getOpacity(state, worldIn, pos);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IWorldReader world, BlockPos pos, EnumFacing face) {
        IBlockState mimic = getActualMimicBlock(world, pos);
        return mimic != null ? mimic.doesSideBlockRendering(world, pos, face) : super.doesSideBlockRendering(state, world, pos, face);
    }


    public void initColorHandler(BlockColors blockColors) {
        blockColors.register((state, world, pos, tintIndex) -> {
            if (world != null) {
                IBlockState mimicBlock = getActualMimicBlock(world, pos);
                return blockColors.getColor(mimicBlock, world, pos, tintIndex);
            }
            return -1;
        }, this);
    }

    /**
     * Get the geometry of the queried face at the given position and state. This is used to decide whether things like
     * buttons are allowed to be placed on the face, or how glass panes connect to the face, among other things.
     * <p>
     * Common values are {@code SOLID}, which is the default, and {@code UNDEFINED}, which represents something that does
     * not fit the other descriptions and will generally cause other things not to connect to the face.
     *
     * @param worldIn
     * @param state
     * @param pos
     * @param face
     * @return an approximation of the form of the given face
     * @deprecated call via {@link IBlockState#getBlockFaceShape(IBlockReader, BlockPos, EnumFacing)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        try {
            return mimicBlock != null ? mimicBlock.getBlockFaceShape(worldIn, pos, face) : super.getBlockFaceShape(worldIn, state, pos, face);
        } catch (Exception e) {
            return BlockFaceShape.SOLID;
        }
    }

    @Override
    public VoxelShape getCollisionShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getCollisionShape(worldIn, pos) : super.getCollisionShape(state, worldIn, pos);
    }

    @Override
    public void onEntityCollision(IBlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.onEntityCollision(worldIn, pos, entityIn);
        } else {
            super.onEntityCollision(state, worldIn, pos, entityIn);
        }
    }

    /**
     * Get the MapColor for this Block and the given BlockState
     *
     * @param state
     * @param worldIn
     * @param pos
//     * @deprecated call via {@link IBlockState#(, BlockPos)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public MapColor getMapColor(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getMapColor(worldIn, pos) : super.getMapColor(state, worldIn, pos);
    }

    /**
     * For all neighbors, have them react to this block's existence, potentially updating their states as needed. For
     * example, fences make their connections to this block if possible and observers pulse if this block was placed in
     * front of their detector
     *
     * @param stateIn
     * @param worldIn
     * @param pos
     * @param flags
     */
    @Override
    public void updateNeighbors(IBlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.updateNeighbors(worldIn, pos, flags);
        } else {
            super.updateNeighbors(stateIn, worldIn, pos, flags);
        }
    }

    /**
     * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
     * from the docs for {@link IWorldWriter#(IBlockState, BlockPos, int)}.
     *
     * @param state
     * @param worldIn
     * @param pos
     * @param flags
     */
    @Override
    public void updateDiagonalNeighbors(IBlockState state, IWorld worldIn, BlockPos pos, int flags) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.updateDiagonalNeighbors(worldIn, pos, flags);
        } else {
            super.updateDiagonalNeighbors(state, worldIn, pos, flags);
        }
    }

    /**
     * Indicate if a material is a normal solid opaque cube
     *
     * @param state
     * @deprecated call via {@link IBlockState#isBlockNormalCube()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    /**
     * Used for nearly all game logic (non-rendering) purposes. Use Forge-provided isNormalCube(IBlockAccess, BlockPos)
     * instead.
     *
     * @param state
     * @deprecated call via {@link IBlockState#isNormalCube()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    /**
     * @param state
     * @deprecated call via {@link IBlockState#isFullCube()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * Determines if the block is solid enough on the top side to support other blocks, like redstone components.
     *
     * @param state
     * @deprecated prefer calling {@link IBlockState#isTopSolid()} wherever possible
     */
    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean allowsMovement(IBlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {

        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.allowsMovement(worldIn, pos, type) : super.allowsMovement(state, worldIn, pos, type);
    }

    /**
     * @param blockState
     * @param worldIn
     * @param pos
     * @deprecated call via {@link IBlockState#(World, BlockPos)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public float getBlockHardness(IBlockState blockState, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getBlockHardness(worldIn, pos) : super.getBlockHardness(blockState, worldIn, pos);
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean isSideInvisible(IBlockState state, IBlockState adjacentBlockState, EnumFacing side) {
        return false;
    }

    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getShape(worldIn, pos) : super.getShape(state, worldIn, pos);
    }

    @Override
    public VoxelShape getRenderShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getRenderShape(worldIn, pos) : super.getRenderShape(state, worldIn, pos);
    }

    @Override
    public VoxelShape getRaytraceShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getRaytraceShape(worldIn, pos) : super.getRaytraceShape(state, worldIn, pos);
    }

    @Override
    public boolean propagatesSkylightDown(IBlockState state, IBlockReader reader, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(reader, pos);
        return mimic != null ? mimic.propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public void randomTick(IBlockState state, World worldIn, BlockPos pos, Random random) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.randomTick(worldIn, pos, random);
        } else {
            super.randomTick(state, worldIn, pos, random);
        }
    }

    @Override
    public void tick(IBlockState state, World worldIn, BlockPos pos, Random random) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.randomTick(worldIn, pos, random);
        } else {
            super.tick(state, worldIn, pos, random);
        }
    }

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
     * this method is unrelated to {@link } and {@link #needsRandomTick}, and will always be called regardless
     * of whether the block can receive random update ticks
     *
     * @param stateIn
     * @param worldIn
     * @param pos
     * @param rand
     */
    @Override
    public void animateTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.randomTick(worldIn, pos, rand);
        } else {
            super.animateTick(stateIn, worldIn, pos, rand);
        }
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     *
     * @param state
     * @param worldIn
     * @param pos
     * @param blockIn
     * @param fromPos
     */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.neighborChanged(worldIn, pos, blockIn, fromPos);
        } else {
            super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        }
    }

    /**
     * Get the hardness of this Block relative to the ability of the given player
     *
     * @param state
     * @param player
     * @param worldIn
     * @param pos
     * @deprecated call via {@link IBlockState#(EntityPlayer, World, BlockPos)} whenever
     * possible. Implementing/overriding is fine.
     */
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getPlayerRelativeBlockHardness(player, worldIn, pos) : super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.onBlockActivated(worldIn, pos, player, hand, side, hitX, hitY, hitZ) : super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ);
    }

    /**
     * Called when the given entity walks on this Block
     *
     * @param worldIn
     * @param pos
     * @param entityIn
     */
    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.getBlock().onEntityWalk(worldIn, pos, entityIn);
        } else {
            super.onEntityWalk(worldIn, pos, entityIn);
        }
    }

    @Nullable
    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public void onBlockClicked(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.onBlockClicked(worldIn, pos, player);
        } else {
            super.onBlockClicked(state, worldIn, pos, player);
        }
    }

    /**
     * @param blockState
     * @param blockAccess
     * @param pos
     * @param side
     * @deprecated call via {@link IBlockState#(, BlockPos, EnumFacing)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public int getWeakPower(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimic = getActualMimicBlock(blockAccess, pos);
        return mimic != null ? mimic.getWeakPower(blockAccess, pos, side) : super.getWeakPower(blockState, blockAccess, pos, side);
    }

    /**
     * @param blockState
     * @param blockAccess
     * @param pos
     * @param side
     * @deprecated call via {@link IBlockState#(, BlockPos, EnumFacing)} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public int getStrongPower(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimic = getActualMimicBlock(blockAccess, pos);
        return mimic != null ? mimic.getStrongPower(blockAccess, pos, side) : super.getStrongPower(blockState, blockAccess, pos, side);
    }

    /**
     * @param state
     * @deprecated call via {@link IBlockState#()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    /**
     * Block's chance to react to a living entity falling on it.
     *
     * @param worldIn
     * @param pos
     * @param entityIn
     * @param fallDistance
     */
    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.getBlock().onFallenUpon(worldIn, pos, entityIn, fallDistance);
        } else {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
    }

    /**
     * Called similar to random ticks, but only when it is raining.
     *
     * @param worldIn
     * @param pos
     */
    @Override
    public void fillWithRain(World worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        if (mimic != null) {
            mimic.getBlock().fillWithRain(worldIn, pos);
        } else {
            super.fillWithRain(worldIn, pos);
        }
    }

    /**
     * @param state
     * @param worldIn
     * @param pos
     * @deprecated call via whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public Vec3d getOffset(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        IBlockState mimic = getActualMimicBlock(worldIn, pos);
        return mimic != null ? mimic.getOffset(worldIn, pos) : super.getOffset(state, worldIn, pos);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockReader world, BlockPos pos, EnumFacing facing, IPlantable plantable) {
        IBlockState mimic = getActualMimicBlock(world, pos);
        return mimic != null ? mimic.canSustainPlant(world, pos, facing, plantable) : super.canSustainPlant(state, world, pos, facing, plantable);
    }
 /* Todo reeval
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        FakeRenderWorld fakeWorld = new FakeRenderWorld();

        IBlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        if (mimicBlock == null) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
        IBlockState sideBlockState = blockAccess.getBlockState(pos.offset(side));
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
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock == null) {
            return super.isNormalCube(state, world, pos);
        }
        try {
            return mimicBlock.getBlock().isNormalCube(mimicBlock, world, pos);
        } catch (Exception var8) {
            return super.isNormalCube(state, world, pos);
        }
    }*/

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        Boolean bright = state.get(ConstructionBlock.BRIGHT);
        Boolean neighborBrightness = state.get(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
        if (bright || neighborBrightness) {
            return 1f;
        }
        return 0.2f;
    }
}
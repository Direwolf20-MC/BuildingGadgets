package com.direwolf20.buildinggadgets.test.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IProperty;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

/**
 * Fake block state without launching Minecraft. This should be used as an identifier and most of the functions and block
 * attributes are broken.
 * <p>
 * All block states created has {@link Material#ROCK} as its material, only {@link #AIR} has {@link Material#AIR}. If a
 * air block state is needed, the pre made air block state should be used instead of creating another one.
 * </p><p>
 * Each newly created block state should have a different ID. The ID that block states have is the only thing it cares
 * about identity and hashing even if they are different objects.
 * </p>
 */
public class UniqueBlockState implements BlockState {

    public static final BlockState AIR = new UniqueBlockState(-1) {
        @Override
        public Material getMaterial() {
            return Material.AIR;
        }
    };

    public static final IFluidState EMPTY_FLUID = new IFluidState() {
        @Override
        public Fluid getFluid() {
            throw new AssertionError("Did not expect Fluid access");
        }

        @Override
        public Collection<IProperty<?>> getProperties() {
            return Collections.emptyList();
        }

        @Override
        public <T extends Comparable<T>> boolean has(IProperty<T> property) {
            return false;
        }

        @Override
        public <T extends Comparable<T>> T get(IProperty<T> property) {
            return null;
        }

        @Override
        public <T extends Comparable<T>, V extends T> IFluidState with(IProperty<T> property, V value) {
            return this;
        }

        @Override
        public <T extends Comparable<T>> IFluidState cycle(IProperty<T> property) {
            return this;
        }

        @Override
        public ImmutableMap<IProperty<?>, Comparable<?>> getValues() {
            return ImmutableMap.of();
        }
    };

    private static int lastId = -1;

    /**
     * Create a new block state object with a specified ID.
     */
    public static UniqueBlockState createWithId(int id) {
        return new UniqueBlockState(id);
    }

    /**
     * Create a new block state object with a new ID. Note that this method does not gardened the ID used haven't
     * been used as a parameter in {@link #createWithId(int)}.
     */
    public static UniqueBlockState createNew() {
        return new UniqueBlockState(++lastId);
    }

    private final int id;

    private UniqueBlockState(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UniqueBlockState{" + id + "}";
    }

    @Override
    public Material getMaterial() {
        return Material.ROCK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueBlockState that = (UniqueBlockState) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Block getBlock() {
        throw new AssertionError("Expected access to BlockState Methods instead of the backing Block!");
    }

    @Override
    public Collection<IProperty<?>> getProperties() {
        return Collections.emptyList();
    }

    @Override
    public <T extends Comparable<T>> T get(IProperty<T> property) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> BlockState cycle(IProperty<T> property) {
        return this;
    }

    @Override
    public boolean canEntitySpawn(Entity entityIn) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public int getOpacity(IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public int getLightValue() {
        return 0;
    }

    @Override
    public boolean isAir() {
        return false;
    }

    @Override
    public boolean useNeighborBrightness(IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public MaterialColor getMapColor(IBlockReader worldIn, BlockPos pos) {
        return MaterialColor.AIR;
    }

    @Override
    public BlockState rotate(Rotation rot) {
        return this;
    }

    @Override
    public BlockState mirror(Mirror mirrorIn) {
        return this;
    }

    @Override
    public boolean isFullCube() {
        return true;
    }

    @Override
    public boolean hasCustomBreakingProgress() {
        return false;
    }

    @Override
    public BlockRenderType getRenderType() {
        return BlockRenderType.MODEL;
    }

    @Override
    public int getPackedLightmapCoords(IWorldReader source, BlockPos pos) {
        return 0;
    }

    @Override
    public float getAmbientOcclusionLightValue() {
        return 0;
    }

    @Override
    public boolean isBlockNormalCube() {
        return true;
    }

    @Override
    public boolean isNormalCube() {
        return true;
    }

    @Override
    public boolean canProvidePower() {
        return false;
    }

    @Override
    public int getWeakPower(IBlockReader blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return false;
    }

    @Override
    public int getComparatorInputOverride(World worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public float getBlockHardness(IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public float getPlayerRelativeBlockHardness(PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public int getStrongPower(IBlockReader blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public PushReaction getPushReaction() {
        return PushReaction.NORMAL;
    }

    @Override
    public boolean isOpaqueCube(IBlockReader worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean isSideInvisible(BlockState state, Direction face) {
        return false;
    }

    @Override
    public VoxelShape getShape(IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getCollisionShape(IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getRenderShape(IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getRaytraceShape(IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public boolean isTopSolid() {
        return true;
    }

    @Override
    public Vec3d getOffset(IBlockReader access, BlockPos pos) {
        return new Vec3d(pos);
    }

    @Override
    public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {
        return false;
    }

    @Override
    public void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {

    }

    @Override
    public void updateNeighbors(IWorld worldIn, BlockPos pos, int flags) {

    }

    @Override
    public void updateDiagonalNeighbors(IWorld worldIn, BlockPos pos, int flags) {

    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, BlockState oldState) {

    }

    @Override
    public void onReplaced(World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {

    }

    @Override
    public void tick(World worldIn, BlockPos pos, Random random) {

    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, Random random) {

    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, Entity entityIn) {

    }

    @Override
    public void dropBlockAsItem(World worldIn, BlockPos pos, int fortune) {

    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, float chancePerItem, int fortune) {

    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, PlayerEntity player) {

    }

    @Override
    public boolean causesSuffocation() {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockPos pos, Direction facing) {
        return null;
    }

    @Override
    public BlockState updatePostPlacement(Direction face, BlockState queried, IWorld worldIn, BlockPos currentPos, BlockPos offsetPos) {
        return this;
    }

    @Override
    public boolean allowsMovement(IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean isReplaceable(BlockItemUseContext useContext) {
        return false;
    }

    @Override
    public boolean isValidPosition(IWorldReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean blockNeedsPostProcessing(IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isIn(Tag<Block> tagIn) {
        return false;
    }

    @Override
    public IFluidState getFluidState() {
        return EMPTY_FLUID;
    }

    @Override
    public boolean needsRandomTick() {
        return false;
    }

    @Override
    public long getPositionRandom(BlockPos pos) {
        return 0;
    }

    //All of these methods should not be used as testing the placement logic should only need to access blockstates

    @Override
    public <T extends Comparable<T>> boolean has(IProperty<T> property) {
        return false;
    }

    @Override
    public <T extends Comparable<T>, V extends T> BlockState with(IProperty<T> property, V value) {
        return this;
    }

    @Override
    public ImmutableMap<IProperty<?>, Comparable<?>> getValues() {
        return ImmutableMap.of();
    }

    @Override
    public BlockState getBlockState() {
        return this;
    }
}

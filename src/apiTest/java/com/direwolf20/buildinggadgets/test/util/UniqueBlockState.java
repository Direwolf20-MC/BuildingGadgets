package com.direwolf20.buildinggadgets.test.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IProperty;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

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
public class UniqueBlockState implements IBlockState {

    public static final IBlockState AIR = new UniqueBlockState(-1) {
        @Override
        public Material getMaterial() {
            return Material.AIR;
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
        return null;
    }

    @Override
    public Collection<IProperty<?>> getProperties() {
        return null;
    }

    @Override
    public <T extends Comparable<T>> T get(IProperty<T> property) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> IBlockState cycle(IProperty<T> property) {
        return null;
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
        return null;
    }

    @Override
    public IBlockState rotate(Rotation rot) {
        return null;
    }

    @Override
    public IBlockState mirror(Mirror mirrorIn) {
        return null;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean hasCustomBreakingProgress() {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType() {
        return null;
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
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean canProvidePower() {
        return false;
    }

    @Override
    public int getWeakPower(IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
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
    public float getPlayerRelativeBlockHardness(EntityPlayer player, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public int getStrongPower(IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @Override
    public EnumPushReaction getPushReaction() {
        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isSideInvisible(IBlockState state, EnumFacing face) {
        return false;
    }

    @Override
    public VoxelShape getShape(IBlockReader worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public VoxelShape getCollisionShape(IBlockReader worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public VoxelShape getRenderShape(IBlockReader worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public VoxelShape getRaytraceShape(IBlockReader worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isTopSolid() {
        return false;
    }

    @Override
    public Vec3d getOffset(IBlockReader access, BlockPos pos) {
        return null;
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
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState oldState) {

    }

    @Override
    public void onReplaced(World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {

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
    public boolean onBlockActivated(World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer player) {

    }

    @Override
    public boolean causesSuffocation() {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockPos pos, EnumFacing facing) {
        return null;
    }

    @Override
    public IBlockState updatePostPlacement(EnumFacing face, IBlockState queried, IWorld worldIn, BlockPos currentPos, BlockPos offsetPos) {
        return null;
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
    public boolean isValidPosition(IWorldReaderBase worldIn, BlockPos pos) {
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
        return null;
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
    public <T extends Comparable<T>, V extends T> IBlockState with(IProperty<T> property, V value) {
        return null;
    }

    @Override
    public ImmutableMap<IProperty<?>, Comparable<?>> getValues() {
        return null;
    }

    @Override
    public IBlockState getBlockState() {
        return null;
    }

    @Override
    public float getSlipperiness(IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return 0;
    }

    @Override
    public int getLightValue(IWorldReader world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean isLadder(IWorldReader world, BlockPos pos, EntityLivingBase entity) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IWorldReader world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public boolean hasTileEntity() {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockReader world) {
        return null;
    }

    @Override
    public boolean canSilkHarvest(IWorldReader world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canHarvestBlock(IBlockReader world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest, IFluidState fluid) {
        return false;
    }

    @Override
    public boolean isBed(IBlockReader world, BlockPos pos, @Nullable EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canCreatureSpawn(IWorldReaderBase world, BlockPos pos, EntitySpawnPlacementRegistry.SpawnPlacementType type, EntityType<? extends EntityLiving> entityType) {
        return false;
    }

    @Nullable
    @Override
    public BlockPos getBedSpawnPosition(IBlockReader world, BlockPos pos, @Nullable EntityPlayer player) {
        return null;
    }

    @Override
    public void setBedOccupied(IWorldReader world, BlockPos pos, EntityPlayer player, boolean occupied) {

    }

    @Override
    public EnumFacing getBedDirection(IWorldReader world, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isBedFoot(IWorldReader world, BlockPos pos) {
        return false;
    }

    @Override
    public void beginLeaveDecay(IWorldReader world, BlockPos pos) {

    }

    @Override
    public boolean isAir(IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IWorldReaderBase world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isReplaceableOreGen(IWorldReader world, BlockPos pos, Predicate<IBlockState> target) {
        return false;
    }

    @Override
    public float getExplosionResistance(IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return 0;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {

    }

    @Override
    public boolean canConnectRedstone(IBlockReader world, BlockPos pos, @Nullable EnumFacing side) {
        return false;
    }

    @Override
    public boolean canPlaceTorchOnTop(IWorldReaderBase world, BlockPos pos) {
        return false;
    }

    @Override
    public ItemStack getPickBlock(RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player) {
        return null;
    }

    @Override
    public boolean isFoliage(IWorldReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean addLandingEffects(WorldServer worldserver, BlockPos pos, IBlockState state2, EntityLivingBase entity, int numberOfParticles) {
        return false;
    }

    @Override
    public boolean addRunningEffects(World world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public boolean addHitEffects(World world, RayTraceResult target, ParticleManager manager) {
        return false;
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return false;
    }

    @Override
    public boolean canSustainPlant(IBlockReader world, BlockPos pos, EnumFacing facing, IPlantable plantable) {
        return false;
    }

    @Override
    public void onPlantGrow(IWorld world, BlockPos pos, BlockPos source) {

    }

    @Override
    public boolean isFertile(IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isBeaconBase(IWorldReader world, BlockPos pos, BlockPos beacon) {
        return false;
    }

    @Override
    public int getExpDrop(IWorldReader world, BlockPos pos, int fortune) {
        return 0;
    }

    @Override
    public IBlockState rotate(IWorld world, BlockPos pos, Rotation direction) {
        return null;
    }

    @Override
    public float getEnchantPowerBonus(IWorldReader world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean recolorBlock(IWorld world, BlockPos pos, EnumFacing facing, EnumDyeColor color) {
        return false;
    }

    @Override
    public void onNeighborChange(IWorldReader world, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public void observedNeighborChange(World world, BlockPos pos, Block changed, BlockPos changedPos) {

    }

    @Override
    public boolean shouldCheckWeakPower(IWorldReader world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean getWeakChanges(IWorldReader world, BlockPos pos) {
        return false;
    }

    @Override
    public ToolType getHarvestTool() {
        return null;
    }

    @Override
    public int getHarvestLevel() {
        return 0;
    }

    @Override
    public boolean isToolEffective(ToolType tool) {
        return false;
    }

    @Override
    public IBlockState getExtendedState(IBlockReader world, BlockPos pos) {
        return null;
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return false;
    }

    @Override
    public SoundType getSoundType(IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return null;
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(IWorldReader world, BlockPos pos, BlockPos beacon) {
        return new float[0];
    }

    @Override
    public Vec3d getFogColor(IWorldReader world, BlockPos pos, Entity entity, Vec3d originalColor, float partialTicks) {
        return null;
    }

    @Override
    public IBlockState getStateAtViewpoint(IWorldReader world, BlockPos pos, Vec3d viewpoint) {
        return null;
    }

    @Override
    public IBlockState getStateForPlacement(EnumFacing facing, IBlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, EnumHand hand) {
        return null;
    }

    @Override
    public boolean canBeConnectedTo(IBlockReader world, BlockPos pos, EnumFacing facing) {
        return false;
    }

    @Override
    public boolean doesSideBlockChestOpening(IBlockReader world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isStickyBlock() {
        return false;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {

    }

    @Override
    public int getFlammability(IBlockReader world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public boolean isFlammable(IBlockReader world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public int getFireSpreadSpeed(IBlockReader world, BlockPos pos, EnumFacing face) {
        return 0;
    }

    @Override
    public boolean isFireSource(IBlockReader world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    @Nullable
    @Override
    public EnumFacing[] getValidRotations(IBlockReader world, BlockPos pos) {
        return new EnumFacing[0];
    }

    @Override
    public boolean isBurning(IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isTopSolid(IWorldReader world, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(IBlockReader world, BlockPos pos, @Nullable EntityLiving entity) {
        return null;
    }
}

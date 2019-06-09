package com.direwolf20.buildinggadgets.common.world;

import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.SimpleBuildContext;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public class FakeDelegationWorld implements IWorld {
    private final IWorld world;
    private Map<BlockPos, BlockState> posToState;
    private Map<BlockPos, TileEntity> posToTile;

    public FakeDelegationWorld(IWorld world) {
        this.world = Objects.requireNonNull(world);
        posToState = new HashMap<>();
        posToTile = new HashMap<>();
    }

    public void addBlock(@Nonnull BlockPos pos, BlockData data) {
        addBlock(null, pos, data);
    }

    public void addBlock(@Nullable IBuildContext context, @Nonnull BlockPos pos, BlockData data) {
        if (data != null)
            data.placeIn(SimpleBuildContext.builderOf(context).build(this), pos);
    }

    public IWorld getDelegate() {
        return world;
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {

    }

    @Override
    public void playEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {

    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entity, AxisAlignedBB axisAlignedBB, @Nullable Predicate<? super Entity> predicate) {
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> aClass, AxisAlignedBB axisAlignedBB, @Nullable Predicate<? super T> predicate) {
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return null;
    }

    @Nullable
    @Override
    public IChunk getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_) {
        return null;
    }

    @Override
    public BlockPos getHeight(Type heightmapType, BlockPos pos) {
        return null;
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean b) {
        return false;
    }

    @Override
    public boolean func_217375_a(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return false;
    }

    @Nullable
    private BlockState getOverriddenState(BlockPos pos) {
        return posToState.get(pos);
    }

    @Nullable
    private TileEntity getOverriddenTile(BlockPos pos) {
        return posToTile.get(pos);
    }

    /**
     * gets the random world seed
     */
    @Override
    public long getSeed() {
        return world.getSeed();
    }

    /**
     * gets the current fullness of the moon expressed as a float between 1.0 and 0.0, in steps of .25
     */
    @Override
    public float getCurrentMoonPhaseFactor() {
        return world.getCurrentMoonPhaseFactor();
    }

    /**
     * calls calculateCelestialAngle
     */
    @Override
    public float getCelestialAngle(float partialTicks) {
        return world.getCelestialAngle(partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return world.getMoonPhase();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return world.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return world.getPendingFluidTicks();
    }

    /**
     * Gets the chunk at the specified location.
     */
    @Override
    public IChunk getChunk(int chunkX, int chunkZ) {
        return world.getChunk(chunkX, chunkZ);
    }

    @Override
    public World getWorld() {
        return world.getWorld();
    }

    /**
     * Returns the world's WorldInfo object
     */
    @Override
    public WorldInfo getWorldInfo() {
        return world.getWorldInfo();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return world.getDifficultyForLocation(pos);
    }

    @Override
    public Difficulty getDifficulty() {
        return world.getDifficulty();
    }

    /**
     * gets the world's chunk provider
     */
    @Override
    public AbstractChunkProvider getChunkProvider() {
        return world.getChunkProvider();
    }


    @Override
    public Random getRandom() {
        return world.getRandom();
    }

    @Override
    public void notifyNeighbors(BlockPos pos, Block blockIn) {
        /*
        left blank as we can't notify Blocks as this isn't a subclass of world and it makes no sense notifying the Blocks in the delegate
        as they won't know about this Block...
        */
    }

    /**
     * Gets the spawn point in the world
     */
    @Override
    public BlockPos getSpawnPoint() {
        return world.getSpawnPoint();
    }


    /**
     * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
     * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
     */
    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).isAir(this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return world.getBiome(pos);
    }

    @Override
    public int getLightFor(LightType type, BlockPos pos) {
        return world.getLightFor(type, pos);
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return world.getLightSubtracted(pos, amount);
    }


    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return world.getHeight(heightmapType, x, z);
    }



    @Override
    public int getSkylightSubtracted() {
        return world.getSkylightSubtracted();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return world.getWorldBorder();
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return world.checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return world.getStrongPower(pos, direction);
    }

    @Override
    public boolean isRemote() {
        return world.isRemote();
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public Dimension getDimension() {
        return world.getDimension();
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return null;
        TileEntity tile = getOverriddenTile(pos);
        if (tile != null) return tile;
        BlockState state = getOverriddenState(pos);
        if (state != null) {
            if (! state.hasTileEntity())
                return null; //if it's overridden, but does not have a tile... well then there is no tile
            tile = state.createTileEntity(this);
            if (tile != null) {
                tile.setPos(pos);
                //tile.setWorld(getWorld());
                posToTile.put(pos, tile);
                return tile;
            }
        }
        return world.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return Blocks.VOID_AIR.getDefaultState();
        BlockState state = getOverriddenState(pos);
        return state != null ? state : world.getBlockState(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState(); // In the end that's what mc does
    }

    @Override
    public int getMaxLightLevel() {
        return world.getMaxLightLevel();
    }

    /**
     * Sets a block state into this world.Flags are as follows:
     * 1 will cause a block update.
     * 2 will send the change to clients.
     * 4 will prevent the block from being re-rendered.
     * 8 will force any re-renders to run on the main thread instead
     * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
     * 32 will prevent neighbor reactions from spawning drops.
     * 64 will signify the block is being moved.
     * Flags can be OR-ed
     */
    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        if (World.isOutsideBuildHeight(pos))
            return false;
        posToState.put(pos, newState);
        return true;
    }



//    fixme: removed in 1.14?
//    @Override
//    public void setLightFor(LightType type, BlockPos pos, int lightValue) {
//        world.setLightFor(type, pos, lightValue);
//    }

    /**
     * Sets a block to air, but also plays the sound and particles and can spawn drops
     */
    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        // adapted from World
        return ! this.getBlockState(pos).isAir(this, pos) && removeBlock(pos, true);
    }
}

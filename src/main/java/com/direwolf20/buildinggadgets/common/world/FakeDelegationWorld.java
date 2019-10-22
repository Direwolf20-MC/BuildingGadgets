package com.direwolf20.buildinggadgets.common.world;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.google.common.base.Preconditions;
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
import java.util.Map.Entry;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public class FakeDelegationWorld implements IWorld {
    private final IWorld delegate;
    private Map<BlockPos, BlockInfo> posToBlock;

    public FakeDelegationWorld(IWorld delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        posToBlock = new HashMap<>();
    }

    public Set<Entry<BlockPos, BlockInfo>> entrySet() {
        return Collections.unmodifiableSet(posToBlock.entrySet());
    }

    public void addBlock(@Nonnull BlockPos pos, BlockData data) {
        addBlock(null, pos, data);
    }

    public void addBlock(@Nullable IBuildContext context, @Nonnull BlockPos pos, BlockData data) {
        if (data != null)
            data.placeIn(SimpleBuildContext.builderOf(context).build(this), pos);
    }

    public IWorld getDelegate() {
        return delegate;
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
        return new ArrayList<>();
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> aClass, AxisAlignedBB axisAlignedBB, @Nullable Predicate<? super T> predicate) {
        return new ArrayList<>();
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public IChunk getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_) {
        return getChunk(p_217353_1_, p_217353_2_);
    }

    @Override
    public BlockPos getHeight(Type heightmapType, BlockPos pos) {
        return getDelegate().getHeight(heightmapType, pos);
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean b) {
        return removeOverride(blockPos);
    }

    @Override
    public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return p_217375_2_.test(getBlockState(p_217375_1_));
    }

    /**
     * gets the random world seed
     */
    @Override
    public long getSeed() {
        return delegate.getSeed();
    }

    /**
     * gets the current fullness of the moon expressed as a float between 1.0 and 0.0, in steps of .25
     */
    @Override
    public float getCurrentMoonPhaseFactor() {
        return delegate.getCurrentMoonPhaseFactor();
    }

    /**
     * calls calculateCelestialAngle
     */
    @Override
    public float getCelestialAngle(float partialTicks) {
        return delegate.getCelestialAngle(partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return delegate.getMoonPhase();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return delegate.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return delegate.getPendingFluidTicks();
    }

    /**
     * Gets the chunk at the specified location.
     */
    @Override
    public IChunk getChunk(int chunkX, int chunkZ) {
        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    public World getWorld() {
        return delegate.getWorld();
    }

    /**
     * Returns the world's WorldInfo object
     */
    @Override
    public WorldInfo getWorldInfo() {
        return delegate.getWorldInfo();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return delegate.getDifficultyForLocation(pos);
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    /**
     * gets the world's chunk provider
     */
    @Override
    public AbstractChunkProvider getChunkProvider() {
        return delegate.getChunkProvider();
    }


    @Override
    public Random getRandom() {
        return delegate.getRandom();
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
        return delegate.getSpawnPoint();
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
        return delegate.getBiome(pos);
    }

    @Override
    public int getLightFor(LightType type, BlockPos pos) {
        return delegate.getLightFor(type, pos);
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return delegate.getLightSubtracted(pos, amount);
    }


    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return delegate.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkylightSubtracted() {
        return delegate.getSkylightSubtracted();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return delegate.checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return delegate.getStrongPower(pos, direction);
    }

    @Override
    public boolean isRemote() {
        return delegate.isRemote();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public Dimension getDimension() {
        return delegate.getDimension();
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return null;
        BlockInfo info = getOverriddenBlock(pos);
        if (info != null)
            return info.getEntity(this);
        return delegate.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return Blocks.VOID_AIR.getDefaultState();
        BlockState state = getOverriddenState(pos);
        return state != null ? state : delegate.getBlockState(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState(); // In the end that's what mc does
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
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
        BlockInfo info = getOverriddenBlock(pos);
        if (info != null) {
            info.setState(newState);
        } else
            posToBlock.put(pos, createInfo(pos, newState));
        return true;
    }

    /**
     * Sets a block to air, but also plays the sound and particles and can spawn drops
     */
    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        // adapted from World
        return ! this.getBlockState(pos).isAir(this, pos) && removeBlock(pos, true);
    }

    //-------------------Extra Methods--------------------

    @Nullable
    public BlockInfo getOverriddenBlock(BlockPos pos) {
        return posToBlock.get(pos);
    }

    @Nullable
    public BlockState getOverriddenState(BlockPos pos) {
        BlockInfo info = getOverriddenBlock(pos);
        return info != null ? info.getState() : null;
    }

    @Nullable
    public TileEntity getOverriddenTile(BlockPos pos) {
        BlockInfo info = getOverriddenBlock(pos);
        return info != null ? info.getEntity(this) : null;
    }

    public void clear() {
        posToBlock.clear();
    }

    public boolean removeOverride(BlockPos pos) {
        BlockInfo info = posToBlock.remove(pos);
        if (info != null) {
            info.onRemove();
            return true;
        }
        return false;
    }

    protected BlockInfo createInfo(BlockPos pos, BlockState state) {
        return new BlockInfo(pos, state);
    }

    public static class BlockInfo {
        private BlockPos pos;
        private BlockState state;
        @Nullable
        private TileEntity entity;

        public BlockInfo(BlockPos pos, BlockState state) {
            this.pos = Objects.requireNonNull(pos);
            this.state = Objects.requireNonNull(state);
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockInfo setPos(BlockPos pos) {
            this.pos = Objects.requireNonNull(pos);
            return this;
        }

        public BlockState getState() {
            return state;
        }

        public BlockInfo setState(BlockState state) {
            Preconditions.checkNotNull(state);
            if (this.state.getBlock() != state.getBlock() || ! state.hasTileEntity()) {
                onRemove();
            }
            this.state = state;
            return this;
        }

        @Nullable
        public TileEntity getEntity(IWorld world) {
            if (entity == null && state.hasTileEntity()) {
                try {
                    entity = state.createTileEntity(world);
                    if (entity != null) {
                        entity.setPos(pos);
                        //if we pass our wrapped world down to this, it will cause it to determine an errornous blockstate...
                        //we'd need to reflect into the te...
                        entity.setWorld(null);
                        entity.onLoad();
                    }
                } catch (Exception e) {
                    BuildingGadgets.LOG.debug("Tile Entity at {} with state {} threw exception whilst creating.", pos, state, e);
                }
            }
            return entity;
        }

        public void onRemove() {
            if (entity != null) {
                try {
                    entity.remove();
                } catch (Exception e) {
                    BuildingGadgets.LOG.debug("Tile Entity at {} with state {} threw exception whilst removing.", pos, state, e);
                }
                entity = null;
            }
        }
    }
}

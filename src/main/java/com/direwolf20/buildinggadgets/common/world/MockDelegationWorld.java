package com.direwolf20.buildinggadgets.common.world;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.google.common.base.Preconditions;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public class MockDelegationWorld implements IWorld {
    private final IWorld delegate;
    private Map<BlockPos, BlockInfo> posToBlock;

    public MockDelegationWorld(IWorld delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        posToBlock = new HashMap<>();
    }

    public Set<Entry<BlockPos, BlockInfo>> entrySet() {
        return Collections.unmodifiableSet(posToBlock.entrySet());
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
    public void levelEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {

    }


    @Override
    public List<Entity> getEntities(@Nullable Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super Entity> p_175674_3_) {
        return new ArrayList<>();
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_) {
        return new ArrayList<>();
    }

    @Override
    public List<? extends PlayerEntity> players() {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public IChunk getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_) {
        return getChunk(p_217353_1_, p_217353_2_);
    }

    @Override
    public BlockPos getHeightmapPos(Type heightmapType, BlockPos pos) {
        return getDelegate().getHeightmapPos(heightmapType, pos);
    }

    @Override
    public DynamicRegistries registryAccess() {
        return null;
    }


    @Override
    public boolean removeBlock(BlockPos blockPos, boolean b) {
        return removeOverride(blockPos);
    }

    @Override
    public boolean isStateAtPosition(BlockPos state, Predicate<BlockState> p_217375_2_) {
        return p_217375_2_.test(getBlockState(state));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return delegate.getMoonPhase();
    }

    @Override
    public ITickList<Fluid> getLiquidTicks() {
        return delegate.getLiquidTicks();
    }

    @Override
    public ITickList<Block> getBlockTicks() {
        return delegate.getBlockTicks();
    }

    /**
     * Gets the chunk at the specified location.
     */
    @Override
    public IChunk getChunk(int chunkX, int chunkZ) {
        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    public IWorldInfo getLevelData() {
        return delegate.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return delegate.getCurrentDifficultyAt(pos);
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    /**
     * gets the world's chunk provider
     */
    @Override
    public AbstractChunkProvider getChunkSource() {
        return delegate.getChunkSource();
    }

    @Override
    public Random getRandom() {
        return delegate.getRandom();
    }

//    @Override
//    public void updateNeighbors(BlockPos p_230547_1_, Block p_230547_2_) {
//                /*
//        left blank as we can't notify Blocks as this isn't a subclass of world and it makes no sense notifying the Blocks in the delegate
//        as they won't know about this Block...
//        */
//    }

    /**
     * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
     * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
     */
    @Override
    public boolean isEmptyBlock(BlockPos pos) {
        return getBlockState(pos).isAir(this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return delegate.getBiome(pos);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return null;
    }

    @Override
    public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return null;
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return delegate.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkyDarken() {
        return delegate.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return null;
    }

// removed 1.16
//    @Override
//    public BiomeManager getBiomeManager() {
//        return null;
//    }
//

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity entityIn, VoxelShape shape) {
        return delegate.isUnobstructed(entityIn, shape);
    }

    @Override
    public int getDirectSignal(BlockPos pos, Direction direction) {
        return delegate.getDirectSignal(pos, direction);
    }


    @Override
    public boolean isClientSide() {
        return delegate.isClientSide();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return delegate.dimensionType();
    }

    @Override
    @Nullable
    public TileEntity getBlockEntity(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return null;
        BlockInfo info = getOverriddenBlock(pos);
        if (info != null)
            return info.getEntity(this);
        return delegate.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return Blocks.VOID_AIR.defaultBlockState();
        BlockState state = getOverriddenState(pos);
        return state != null ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState(); // In the end that's what mc does
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public boolean setBlock(BlockPos p_241211_1_, BlockState p_241211_2_, int p_241211_3_, int p_241211_4_) {
        return false;
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
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
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

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return false;
    }

    // todo: 1.16 removed.
//    @Override
//    public boolean destroyBlock(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
//        return false;
//    }

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

    @Override
    public float getBrightness(BlockPos pos) {
        return delegate.getBrightness(pos);
    }

    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return delegate.getShade(p_230487_1_, p_230487_2_);
    }

    @Override
    public WorldLightManager getLightEngine() {
        return delegate.getLightEngine();
    }

    @Tainted(reason = "Pointless system, also, uncommented...")
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
                        entity.setPosition(pos);
                        //if we pass our wrapped world down to this, it will cause it to determine an errornous blockstate...
                        //we'd need to reflect into the te...
                        entity.setLevelAndPosition(null, pos);
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
                    entity.setRemoved();
                } catch (Exception e) {
                    BuildingGadgets.LOG.debug("Tile Entity at {} with state {} threw exception whilst removing.", pos, state, e);
                }
                entity = null;
            }
        }
    }
}

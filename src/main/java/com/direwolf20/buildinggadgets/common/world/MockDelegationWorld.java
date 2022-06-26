package com.direwolf20.buildinggadgets.common.world;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.google.common.base.Preconditions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public class MockDelegationWorld implements LevelAccessor {
    private final LevelAccessor delegate;
    private Map<BlockPos, BlockInfo> posToBlock;

    public MockDelegationWorld(LevelAccessor delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        posToBlock = new HashMap<>();
    }

    public Set<Entry<BlockPos, BlockInfo>> entrySet() {
        return Collections.unmodifiableSet(posToBlock.entrySet());
    }

    public LevelAccessor getDelegate() {
        return delegate;
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent soundIn, SoundSource category, float volume, float pitch) {

    }

    @Override
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {

    }

    @Override
    public void levelEvent(@Nullable Player p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {

    }

    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_) {

    }

    @Override
    public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {

    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB axisAlignedBB, @Nullable Predicate<? super Entity> predicate) {
        return new ArrayList<>();
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> p_151464_, AABB p_151465_, Predicate<? super T> p_151466_) {
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> p_45979_, AABB p_45980_, Predicate<? super T> p_45981_) {
        return List.of();
    }

    @Override
    public List<? extends Player> players() {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_) {
        return getChunk(p_217353_1_, p_217353_2_);
    }

    @Override
    public BlockPos getHeightmapPos(Types heightmapType, BlockPos pos) {
        return getDelegate().getHeightmapPos(heightmapType, pos);
    }

    @Override
    public RegistryAccess registryAccess() {
        return null;
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean b) {
        return removeOverride(blockPos);
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return p_217375_2_.test(getBlockState(p_217375_1_));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos p_151584_, Predicate<FluidState> p_151585_) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return delegate.getMoonPhase();
    }

    /**
     * Gets the chunk at the specified location.
     */
    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ) {
        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    public long nextSubTickCount() {
        return delegate.nextSubTickCount();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return delegate.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return delegate.getFluidTicks();
    }

    @Override
    public LevelData getLevelData() {
        return delegate.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return delegate.getCurrentDifficultyAt(pos);
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.delegate.getServer();
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    /**
     * gets the world's chunk provider
     */
    @Override
    public ChunkSource getChunkSource() {
        return delegate.getChunkSource();
    }


    @Override
    public RandomSource getRandom() {
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
        return getBlockState(pos).isAir();
    }

    @Override
    public Holder<Biome> getBiome(BlockPos p_204167_) {
        return LevelAccessor.super.getBiome(p_204167_);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        return delegate.getUncachedNoiseBiome(p_204159_, p_204160_, p_204161_);
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
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
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (this.delegate.isOutsideBuildHeight(pos))
            return null;
        BlockInfo info = getOverriddenBlock(pos);
        if (info != null)
            return info.getEntity(this);
        return delegate.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.delegate.isOutsideBuildHeight(pos))
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
        if (this.delegate.isOutsideBuildHeight(pos))
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
        return !this.getBlockState(pos).isAir() && removeBlock(pos, true);
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
    public BlockEntity getOverriddenTile(BlockPos pos) {
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
    public int getBrightness(LightLayer p_45518_, BlockPos p_45519_) {
        return delegate.getBrightness(p_45518_, p_45519_);
    }
    
    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Tainted(reason = "Pointless system, also, uncommented...")
    public static class BlockInfo {
        private BlockPos pos;
        private BlockState state;
        @Nullable
        private BlockEntity entity;

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
            if (this.state.getBlock() != state.getBlock() || !state.hasBlockEntity()) {
                onRemove();
            }
            this.state = state;
            return this;
        }

        @Nullable
        public BlockEntity getEntity(LevelAccessor world) {
            if (entity == null && state.hasBlockEntity()) {
                try {
                    entity = ((EntityBlock) state.getBlock()).newBlockEntity(pos, state);
                    if (entity != null) {

                        //if we pass our wrapped world down to this, it will cause it to determine an errornous blockstate...
                        //we'd need to reflect into the te...

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

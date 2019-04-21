package com.direwolf20.buildinggadgets.common.world;

import com.direwolf20.buildinggadgets.api.template.building.BlockData;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.SimpleBuildContext;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public class FakeDelegationWorld implements IWorld {
    private final IWorld world;
    private Map<BlockPos, IBlockState> posToState;
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

    @Nullable
    private IBlockState getOverriddenState(BlockPos pos) {
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

    @Override
    public IChunk getChunkDefault(BlockPos pos) {
        return world.getChunkDefault(pos);
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
    public EnumDifficulty getDifficulty() {
        return world.getDifficulty();
    }

    /**
     * gets the world's chunk provider
     */
    @Override
    public IChunkProvider getChunkProvider() {
        return world.getChunkProvider();
    }

    /**
     * Returns this world's current save handler
     */
    @Override
    public ISaveHandler getSaveHandler() {
        return world.getSaveHandler();
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
     * Plays the specified sound for a player at the center of the given block position.
     */
    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        world.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void spawnParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        world.spawnParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
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
    public int getLightFor(EnumLightType type, BlockPos pos) {
        return world.getLightFor(type, pos);
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return world.getLightSubtracted(pos, amount);
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return world.isChunkLoaded(x, z, allowEmpty);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return canBlockSeeSky(pos); //Delegate to default Method in IWorldReaderBase
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return world.getHeight(heightmapType, x, z);
    }

    /**
     * Gets the closest player to the entity within the specified distance.
     */
    @Override
    @Nullable
    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
        return world.getClosestPlayerToEntity(entityIn, distance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return world.getNearestPlayerNotCreative(entityIn, distance);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        return world.getClosestPlayer(posX, posY, posZ, distance, spectator);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return world.getClosestPlayer(x, y, z, distance, predicate);
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
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
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
        IBlockState state = getOverriddenState(pos);
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
    public IBlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos))
            return Blocks.VOID_AIR.getDefaultState();
        IBlockState state = getOverriddenState(pos);
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

    @Override
    @Nullable
    public WorldSavedDataStorage getMapStorage() {
        return world.getMapStorage();
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
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (World.isOutsideBuildHeight(pos))
            return false;
        posToState.put(pos, newState);
        return true;
    }

    /**
     * Called when an entity is spawned in the world. This includes players.
     */
    @Override
    public boolean spawnEntity(Entity entityIn) {
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos pos) {
        IFluidState state = getFluidState(pos);
        boolean res = this.setBlockState(pos, state.getBlockState(), 3);
        if (res && posToTile.containsKey(pos))
            posToTile.remove(pos)
                    .remove();
        return res;
    }

    @Override
    public void setLightFor(EnumLightType type, BlockPos pos, int lightValue) {
        world.setLightFor(type, pos, lightValue);
    }

    /**
     * Sets a block to air, but also plays the sound and particles and can spawn drops
     */
    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        // adapted from World
        return ! this.getBlockState(pos).isAir(this, pos) && removeBlock(pos);
    }
}

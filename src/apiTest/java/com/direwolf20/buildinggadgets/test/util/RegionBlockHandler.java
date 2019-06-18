package com.direwolf20.buildinggadgets.test.util;

import com.direwolf20.buildinggadgets.api.building.Region;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

public class RegionBlockHandler implements IWorld {

    private final Random rand = new Random();

    private final Region region;
    private Map<BlockPos, IBlockState> blocks = new HashMap<>();

    public RegionBlockHandler(Region region) {
        this.region = region;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (region.contains(pos)) {
            blocks.put(pos, newState);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public IBlockState getBlockState(BlockPos pos) {
        IBlockState stateCandidate = region.contains(pos) ? blocks.get(pos) : UniqueBlockState.AIR;
        return stateCandidate == null ? UniqueBlockState.AIR : stateCandidate;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return ! region.contains(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing facing) {
        return getBlockState(pos).getStrongPower(this, pos, facing);
    }

    @Override
    public boolean removeBlock(BlockPos pos) {
        return setBlockState(pos, UniqueBlockState.AIR, - 1);
    }

    @Override
    public int getLightFor(EnumLightType type, BlockPos pos) {
        return 0;
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return 0;
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return false;
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return 0;
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public int getSkylightSubtracted() {
        return 0;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public Dimension getDimension() {
        return null;
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Deprecated
    @Override
    public Biome getBiome(BlockPos pos) {
        return null;
    }

    @Override
    public long getSeed() {
        return 0;
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return null;
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public WorldInfo getWorldInfo() {
        return null;
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return null;
    }

    @Override
    public IChunkProvider getChunkProvider() {
        return null;
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return null;
    }

    @Override
    public Random getRandom() {
        return rand;
    }

    @Override
    public void notifyNeighbors(BlockPos pos, Block blockIn) {

    }

    @Override
    public BlockPos getSpawnPoint() {
        return null;
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void spawnParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {

    }

    @Nullable
    @Override
    public WorldSavedDataStorage getMapStorage() {
        return null;
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return false;
    }

    @Override
    public void setLightFor(EnumLightType type, BlockPos pos, int lightValue) {

    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return false;
    }
}

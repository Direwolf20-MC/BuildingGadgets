package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class DelegationWorld extends World {
    private static final Scoreboard SCOREBOARD = new Scoreboard();
    private World delegate;
    private Map<BlockPos, MockDelegationWorld.BlockInfo> posToBlock;

    public DelegationWorld() {
        super(Minecraft.getInstance().world.getWorldInfo(), Minecraft.getInstance().world.getDimensionKey(), Minecraft.getInstance().world.getDimensionType(), Minecraft.getInstance().world.getWorldProfiler(), true, true, 0L);
        this.delegate = Minecraft.getInstance().world;
    }

    public Set<Map.Entry<BlockPos, MockDelegationWorld.BlockInfo>> entrySet() {
        return Collections.unmodifiableSet(this.posToBlock.entrySet());
    }

    @Nullable
    public MockDelegationWorld.BlockInfo getOverriddenBlock(BlockPos pos) {
        return this.posToBlock.get(pos);
    }

    @Nullable
    public BlockState getOverriddenState(BlockPos pos) {
        MockDelegationWorld.BlockInfo info = this.getOverriddenBlock(pos);
        return info != null
            ? info.getState()
            : null;
    }

    @Nullable
    public TileEntity getOverriddenTile(BlockPos pos) {
        MockDelegationWorld.BlockInfo info = this.getOverriddenBlock(pos);
        return info != null
            ? info.getEntity(this)
            : null;
    }

    public void clear() {
        this.posToBlock.clear();
    }

    public boolean removeOverride(BlockPos pos) {
        MockDelegationWorld.BlockInfo info = this.posToBlock.remove(pos);
        if (info != null) {
            info.onRemove();
            return true;
        }
        return false;
    }

    protected MockDelegationWorld.BlockInfo createInfo(BlockPos pos, BlockState state) {
        return new MockDelegationWorld.BlockInfo(pos, state);
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean b) {
        return this.removeOverride(blockPos);
    }

    @Override
    public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return p_217375_2_.test(this.getBlockState(p_217375_1_));
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return this.getBlockState(pos).isAir(this, pos);
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos)) {
            return null;
        }
        MockDelegationWorld.BlockInfo info = this.getOverriddenBlock(pos);
        if (info != null) {
            return info.getEntity(this);
        }
        return this.delegate.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos)) {
            return Blocks.VOID_AIR.getDefaultState();
        }
        BlockState state = this.getOverriddenState(pos);
        return state != null
            ? state
            : Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState(); // In the end that's what mc does
    }

    @Override
    public boolean setBlockState(BlockPos p_241211_1_, BlockState p_241211_2_, int p_241211_3_, int p_241211_4_) {
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
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        if (World.isOutsideBuildHeight(pos)) {
            return false;
        }
        MockDelegationWorld.BlockInfo info = this.getOverriddenBlock(pos);
        if (info != null) {
            info.setState(newState);
        } else {
            this.posToBlock.put(pos, this.createInfo(pos, newState));
        }
        return true;
    }

    /**
     * Sets a block to air, but also plays the sound and particles and can spawn drops
     */
    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        // adapted from World
        return !this.getBlockState(pos).isAir(this, pos) && this.removeBlock(pos, true);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return false;
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch) {

    }

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    @Nullable
    @Override
    public MapData getMapData(String mapName) {
        return null;
    }

    @Override
    public void registerMapData(MapData mapDataIn) {

    }

    @Override
    public int getNextMapId() {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return SCOREBOARD;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    public ITagCollectionSupplier getTags() {
        return null;
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return this.delegate.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return this.delegate.getPendingFluidTicks();
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return this.delegate.getChunkProvider();
    }

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {

    }

    @Override
    public DynamicRegistries func_241828_r() {
        return null;
    }

    //getBrightness
    @Override
    public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_) {
        return 0;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return new ArrayList<>();
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return null;
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity tileEntityIn) {
    }

    @Override
    public void tickBlockEntities() {
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
    }


    @Override
    public void removeTileEntity(BlockPos pos) {
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public CrashReportCategory fillCrashReport(CrashReport report) {
        CrashReportCategory crashreportcategory = report.makeCategory("Building Gadgets");
        crashreportcategory.addDetail(
            "DelegationWorld", () -> {
            }
        );
        return crashreportcategory;
    }

    @Override
    public DimensionType getDimensionType() {
        return OVERWORLD;
    }
}

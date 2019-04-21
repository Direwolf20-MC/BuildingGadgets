package com.direwolf20.buildinggadgets.util;

import com.direwolf20.buildinggadgets.common.building.Region;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * A fake world that has a set of positions "filled" with some type of block and the rest will be air ({@link UniqueBlockState#AIR}).
 * This class should be used for tests that checks for range overflow or need a world for reference.
 *
 * <p>"Region" means the effective range is limited and "View" indicates this class is immutable.</p>
 */
public class RegionBlockView implements IWorldReaderBase {

    private Region region;
    private IBlockState state;

    public RegionBlockView(Region region, IBlockState state) {
        this.region = region;
        this.state = state;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return region.contains(pos) ? state : UniqueBlockState.AIR;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return !region.contains(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing facing) {
        return getBlockState(pos).getStrongPower(this, pos, facing);
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

}

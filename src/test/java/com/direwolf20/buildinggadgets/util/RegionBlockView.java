package com.direwolf20.buildinggadgets.util;

import com.direwolf20.buildinggadgets.common.building.Region;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class RegionBlockView implements IBlockAccess {

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
    public WorldType getWorldType() {
        return WorldType.DEBUG_ALL_BLOCK_STATES;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing facing, boolean b) {
        return getBlockState(pos).isSideSolid(this, pos, facing);
    }

    @Deprecated
    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Deprecated
    @Override
    public int getCombinedLight(BlockPos pos, int i) {
        return 0;
    }

    @Deprecated
    @Override
    public Biome getBiome(BlockPos pos) {
        return null;
    }

}

package com.direwolf20.buildinggadgets.common.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeRenderWorld implements IBlockReader {
    private Map<BlockPos, IBlockState> posMap = new HashMap<BlockPos, IBlockState>();
    private World realWorld;


    public void setState(World rWorld, IBlockState setBlock, BlockPos coordinate) {
        this.realWorld = rWorld;
        posMap.put(coordinate, setBlock);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return realWorld.getTileEntity(pos);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return posMap.containsKey(pos) ? posMap.get(pos) : realWorld.getBlockState(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return realWorld.getCombinedLight(pos, lightValue);
    }

    public boolean isAirBlock(BlockPos pos) {
        if (posMap.containsKey(pos)) {
            return posMap.get(pos).equals(Blocks.AIR.getDefaultState());
        }
        return realWorld.isAirBlock(pos);
    }

    public Biome getBiome(BlockPos pos) {
        return realWorld.getBiome(pos);
    }

    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    public WorldType getWorldType() {
        return realWorld.getWorldType();
    }
}

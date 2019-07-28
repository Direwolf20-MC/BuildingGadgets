package com.direwolf20.buildinggadgets.client.renderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeTERWorld implements IBlockReader {

    private Map<BlockState, TileEntityRenderer<TileEntity>> terMap = new HashMap<>();
    private Map<BlockState, TileEntity> teMap = new HashMap<>();

    public TileEntityRenderer<TileEntity> getTER(BlockState state, World realWorld) {
        if (terMap.containsKey(state)) {
            return terMap.get(state);
        } else {
            TileEntity te = getTE(state, realWorld);
            TileEntityRenderer<TileEntity> teRender = TileEntityRendererDispatcher.instance.getRenderer(te);
            terMap.put(state, teRender);
            return terMap.get(state);
        }
    }

    public TileEntity getTE(BlockState state, World realWorld) {
        if (teMap.containsKey(state)) {
            return teMap.get(state);
        } else {
            TileEntity te = state.getBlock().createTileEntity(state, this);
            te.setWorld(realWorld);
            teMap.put(state, te);
            return teMap.get(state);
        }
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return null;
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return null;
    }
}

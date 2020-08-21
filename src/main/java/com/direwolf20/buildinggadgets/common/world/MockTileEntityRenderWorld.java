package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MockTileEntityRenderWorld implements IBlockReader {

    private final Map<BlockState, TileEntityRenderer<TileEntity>> tileEntityRenders = new HashMap<>();
    private final Map<BlockState, TileEntity> tileEntities = new HashMap<>();

    public TileEntityRenderer<TileEntity> getTileEntityRender(BlockState state) {
        if (!tileEntityRenders.containsKey(state)) {
            TileEntity te = getTileEntity(state);
            TileEntityRenderer<TileEntity> teRender = TileEntityRendererDispatcher.instance.getRenderer(te);
            tileEntityRenders.put(state, teRender);
        }

        return tileEntityRenders.get(state);
    }

    public TileEntity getTileEntity(BlockState state) {
        if (!tileEntities.containsKey(state)) {
            TileEntity te = state.getBlock().createTileEntity(state, this);
            if (te == null) {
                return tileEntities.get(state);
            }

            tileEntities.put(state, te);
        }

        return tileEntities.get(state);
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
    public FluidState getFluidState(BlockPos pos) {
        return null;
    }
}

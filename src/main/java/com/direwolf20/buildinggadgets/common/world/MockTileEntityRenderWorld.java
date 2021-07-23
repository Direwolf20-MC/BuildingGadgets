package com.direwolf20.buildinggadgets.common.world;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Tainted(reason = "Shouldn't exist")
public class MockTileEntityRenderWorld implements BlockGetter {

    private final Map<BlockState, BlockEntityRenderer<BlockEntity>> tileEntityRenders = new HashMap<>();
    private final Map<BlockState, BlockEntity> tileEntities = new HashMap<>();

    public BlockEntityRenderer<BlockEntity> getTileEntityRender(BlockState state) {
        if (!tileEntityRenders.containsKey(state)) {
            BlockEntity te = getTileEntity(state);
            BlockEntityRenderer<BlockEntity> teRender = BlockEntityRenderDispatcher.instance.getRenderer(te);
            tileEntityRenders.put(state, teRender);
        }

        return tileEntityRenders.get(state);
    }

    public BlockEntity getTileEntity(BlockState state) {
        if (!tileEntities.containsKey(state)) {
            BlockEntity te = state.getBlock().createTileEntity(state, this);
            if (te == null) {
                return tileEntities.get(state);
            }

            tileEntities.put(state, te);
        }

        return tileEntities.get(state);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
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

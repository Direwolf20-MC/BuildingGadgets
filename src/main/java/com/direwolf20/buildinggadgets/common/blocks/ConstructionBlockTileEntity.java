package com.direwolf20.buildinggadgets.common.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class ConstructionBlockTileEntity extends TileEntity {
    private IBlockState blockState;
    private IBlockState actualBlockState;

    public boolean setBlockState(IBlockState state) {
        blockState = state;
        markDirtyClient();
        return true;
    }

    public boolean setActualBlockState(IBlockState state) {
        actualBlockState = state;
        markDirtyClient();
        return true;
    }

    @Nullable
    public IBlockState getBlockState() {
        if (blockState == null || blockState == Blocks.AIR.getDefaultState()) {
            return null;
        }
        return blockState;
    }

    @Nullable
    public IBlockState getActualBlockState() {
        if (actualBlockState == null || actualBlockState == Blocks.AIR.getDefaultState()) {
            return null;
        }
        return actualBlockState;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        blockState = NBTUtil.readBlockState(compound.getCompoundTag("blockState"));
        actualBlockState = NBTUtil.readBlockState(compound.getCompoundTag("actualBlockState"));
        markDirtyClient();
    }

    private void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = getBlockState();
        NBTTagCompound tagCompound = packet.getNbtCompound();
        super.onDataPacket(net, packet);
        readFromNBT(tagCompound);
        if (world.isRemote) {
            // If needed send a render update.
            if (getBlockState() != oldMimicBlock) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (blockState != null) {
            NBTTagCompound blockStateTag = new NBTTagCompound();
            NBTTagCompound actualBlockStateTag = new NBTTagCompound();
            if (blockState != null) {
                NBTUtil.writeBlockState(blockStateTag, blockState);
                compound.setTag("blockState", blockStateTag);
            }
            if (actualBlockState != null) {
                NBTUtil.writeBlockState(actualBlockStateTag, actualBlockState);
                compound.setTag("actualBlockState", actualBlockStateTag);
            }
        }
        return compound;
    }
}

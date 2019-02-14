package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingObjects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class ConstructionBlockTileEntity extends TileEntity {
    private IBlockState blockState;
    private IBlockState actualBlockState;

    public ConstructionBlockTileEntity() {
        super(BuildingObjects.CONSTRUCTION_BLOCK_TYPE);
    }

    public void setBlockState(IBlockState state, IBlockState actualState) {
        blockState = state;
        actualBlockState = state;
        markDirtyClient();
    }

    @Nonnull
    public IBlockState getBlockState() {
        if (blockState == null) {
            return Blocks.AIR.getDefaultState();
        }
        return blockState;
    }

    @Nonnull
    public IBlockState getActualBlockState() {
        if (actualBlockState == null) {
            return Blocks.AIR.getDefaultState();
        }
        return actualBlockState;
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);
        blockState = NBTUtil.readBlockState(compound.getCompound("blockState"));
        actualBlockState = NBTUtil.readBlockState(compound.getCompound("actualBlockState"));
        markDirtyClient();
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        if (blockState != null) {
            compound.setTag("blockState", NBTUtil.writeBlockState(blockState));
            if (actualBlockState != null) {
                compound.setTag("actualBlockState", NBTUtil.writeBlockState(actualBlockState));
            }
        }
        return super.write(compound);
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
        write(updateTag);
        return updateTag;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        write(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = getBlockState();
        NBTTagCompound tagCompound = packet.getNbtCompound();
        super.onDataPacket(net, packet);
        read(tagCompound);
        if (world.isRemote) {
            // If needed send a render update.
            if (!getBlockState().equals(oldMimicBlock)) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }
}

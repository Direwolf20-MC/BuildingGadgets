package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.registry.objects.BGTileEntities;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class ConstructionBlockTileEntity extends TileEntity {
    private BlockState blockState;
    private BlockState actualBlockState;

    public ConstructionBlockTileEntity() {
        super(BGTileEntities.CONSTRUCTION_BLOCK_TYPE);
    }

    public void setBlockState(BlockState state, BlockState actualState) {
        blockState = state;
        actualBlockState = state;
        markDirtyClient();
    }

    @Nonnull
    public BlockState getBlockState() {
        if (blockState == null) {
            return Blocks.AIR.getDefaultState();
        }
        return blockState;
    }

    @Nonnull
    public BlockState getActualBlockState() {
        if (actualBlockState == null) {
            return Blocks.AIR.getDefaultState();
        }
        return actualBlockState;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        blockState = NBTUtil.readBlockState(compound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE));
        actualBlockState = NBTUtil.readBlockState(compound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL));
        markDirtyClient();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if (blockState != null) {
            compound.put(NBTKeys.TE_CONSTRUCTION_STATE, NBTUtil.writeBlockState(blockState));
            if (actualBlockState != null)
                compound.put(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL, NBTUtil.writeBlockState(actualBlockState));
        }
        return super.write(compound);
    }

    private void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            BlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        write(updateTag);
        return updateTag;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        write(nbtTag);
        return new SUpdateTileEntityPacket(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        BlockState oldMimicBlock = getBlockState();
        CompoundNBT tagCompound = packet.getNbtCompound();
        super.onDataPacket(net, packet);
        read(tagCompound);
        if (world.isRemote) {
            // If needed send a render update.
            if (!getBlockState().equals(oldMimicBlock)) {
                world.markChunkDirty(getPos(), this.getTileEntity());
            }
        }
    }
}

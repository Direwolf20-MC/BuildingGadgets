package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;

public class ConstructionBlockTileEntity extends TileEntity {

    private BlockData blockState;
    private BlockData actualBlockState;
    public static final ModelProperty<BlockState> FACADE_STATE = new ModelProperty<>();

    public ConstructionBlockTileEntity() {
        super(OurTileEntities.CONSTRUCTION_BLOCK_TILE_ENTITY.get());
    }

    public void setBlockState(BlockData state, BlockData actualState) {
        blockState = state;
        actualBlockState = actualState;
        markDirtyClient();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        BlockState state = getActualBlockData().getState();
        //TODO: query simulated Tile, if exists, and relay model data...
        return new ModelDataMap.Builder().withInitial(FACADE_STATE, state).build();
    }

    @Nonnull
    @Override
    public BlockState getBlockState() {
        return getConstructionBlockData().getState();
    }

    @Nonnull
    public BlockData getConstructionBlockData() {
        if (blockState == null)
            return BlockData.AIR;
        return blockState;
    }

    @Nonnull
    public BlockData getActualBlockData() {
        if (actualBlockState == null)
            return BlockData.AIR;
        return actualBlockState;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        super.deserializeNBT(compound);
        blockState = BlockData.tryDeserialize(compound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE), true);
        actualBlockState = BlockData.tryDeserialize(compound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL), true);
        markDirtyClient();
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        if (blockState != null) {
            compound.put(NBTKeys.TE_CONSTRUCTION_STATE, blockState.serialize(true));
            if (actualBlockState != null)
                compound.put(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL, actualBlockState.serialize(true));
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

    @Nonnull
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
        BlockData oldMimicBlock = getConstructionBlockData();
        CompoundNBT tagCompound = packet.getNbtCompound();
        super.onDataPacket(net, packet);
        deserializeNBT(tagCompound);

        if (world != null && world.isRemote) {
            // If needed send a render update.
            if (! getConstructionBlockData().equals(oldMimicBlock)) {
                world.markChunkDirty(getPos(), this.getTileEntity());
            }
        }
    }
}

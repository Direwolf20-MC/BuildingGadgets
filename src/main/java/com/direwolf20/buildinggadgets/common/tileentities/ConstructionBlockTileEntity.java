package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ConstructionBlockTileEntity extends BlockEntity {

    private BlockData blockState;
    public static final ModelProperty<BlockState> FACADE_STATE = new ModelProperty<>();

    public ConstructionBlockTileEntity(BlockPos pos, BlockState state) {
        super(OurTileEntities.CONSTRUCTION_BLOCK_TILE_ENTITY.get(), pos, state);
    }

    public void setBlockState(BlockData state) {
        blockState = state;
        markDirtyClient();
    }

    // TODO: query simulated Tile, if exists, and relay model data...
    @Override
    public @NotNull ModelData getModelData() {
        if (blockState == null) {
            return super.getModelData();
        }

        BlockState state = blockState.getState();
        return ModelData.builder().with(FACADE_STATE, state).build();
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

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        blockState = BlockData.tryDeserialize(nbt.getCompound(NBTKeys.TE_CONSTRUCTION_STATE), true);
        markDirtyClient();
    }

    @Nonnull
    @Override
    protected void saveAdditional(CompoundTag compound) {
        if (blockState != null) {
            compound.put(NBTKeys.TE_CONSTRUCTION_STATE, blockState.serialize(true));
        }
        super.saveAdditional(compound);
    }

    private void markDirtyClient() {
        setChanged();
        if (getLevel() != null) {
            BlockState state = getLevel().getBlockState(getBlockPos());
            getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        saveAdditional(updateTag);
        return updateTag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag nbtTag = new CompoundTag();
        saveAdditional(nbtTag);
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        BlockData oldMimicBlock = getConstructionBlockData();
        CompoundTag tagCompound = packet.getTag();
        super.onDataPacket(net, packet);
        deserializeNBT(tagCompound);

        if (level != null && level.isClientSide) {
            // If needed send a render update.
            if (! getConstructionBlockData().equals(oldMimicBlock)) {
                level.blockEntityChanged(getBlockPos());
            }
        }
    }
}

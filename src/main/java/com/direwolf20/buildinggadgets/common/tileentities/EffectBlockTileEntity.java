package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Tainted(reason = "Used blockData and a stupid non-centralised callback system")
public class EffectBlockTileEntity extends BlockEntity implements TickingBlockEntity {
    /**
     * Even though this is called "rendered", is will be used for replacement under normal conditions.
     */
    private BlockData renderedBlock;
    /**
     * A copy of the target block, used for inheriting data for {@link Mode#REPLACE}
     */
    private BlockData sourceBlock;

    private Mode mode = null;
    private boolean usePaste;

    private int ticks;

    public EffectBlockTileEntity() {
        super(OurTileEntities.EFFECT_BLOCK_TILE_ENTITY.get());
    }

    public void initializeData(BlockState curState, @Nullable BlockEntity te, BlockData replacementBlock, Mode mode, boolean usePaste) {
        // Minecraft will reuse a tile entity object at a location where the block got removed, but the modification is still buffered, and the block got restored again
        // If we don't reset this here, the 2nd phase of REPLACE will simply finish immediately because the tile entity object is reused
        this.ticks = 0;
        // Again we don't check if the data has been set or not because there is a chance that this tile object gets reused
        this.sourceBlock = replacementBlock;

        this.mode = mode;
        this.usePaste = usePaste;

        if (mode == Mode.REPLACE)
            this.renderedBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : TileSupport.createBlockData(curState, te);
        else
            this.renderedBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : replacementBlock;
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks >= getLifespan()) {
            complete();
        }
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    private void complete() {
        if (level == null || level.isClientSide || mode == null || renderedBlock == null)
            return;

        mode.onBuilderRemoved(this);
    }

    public BlockData getRenderedBlock() {
        return renderedBlock;
    }

    public BlockData getSourceBlock() {
        return sourceBlock;
    }

    public Mode getReplacementMode() {
        return mode;
    }

    public boolean isUsingPaste() {
        return usePaste;
    }

    public int getTicksExisted() {
        return ticks;
    }

    public int getLifespan() {
        return 20;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return new ClientboundBlockEntityDataPacket(worldPosition, 0, getUpdateTag());
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        deserializeNBT(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        deserializeNBT(pkt.getTag());
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        if (mode != null && renderedBlock != null && sourceBlock != null) {
            compound.putInt(NBTKeys.GADGET_TICKS, ticks);
            compound.putInt(NBTKeys.GADGET_MODE, mode.ordinal());
            compound.put(NBTKeys.GADGET_REPLACEMENT_BLOCK, renderedBlock.serialize(true));
            compound.put(NBTKeys.GADGET_SOURCE_BLOCK, sourceBlock.serialize(true));
            compound.putBoolean(NBTKeys.GADGET_USE_PASTE, usePaste);
        }
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundTag nbt) {
        super.load(state, nbt);

        if (nbt.contains(NBTKeys.GADGET_TICKS, NBT.TAG_INT) &&
                nbt.contains(NBTKeys.GADGET_MODE, NBT.TAG_INT) &&
                nbt.contains(NBTKeys.GADGET_SOURCE_BLOCK, NBT.TAG_COMPOUND) &&
                nbt.contains(NBTKeys.GADGET_REPLACEMENT_BLOCK, NBT.TAG_COMPOUND) &&
                nbt.contains(NBTKeys.GADGET_USE_PASTE)) {

            ticks = nbt.getInt(NBTKeys.GADGET_TICKS);
            mode = Mode.VALUES[nbt.getInt(NBTKeys.GADGET_MODE)];
            renderedBlock = BlockData.tryDeserialize(nbt.getCompound(NBTKeys.GADGET_REPLACEMENT_BLOCK), true);
            sourceBlock = BlockData.tryDeserialize(nbt.getCompound(NBTKeys.GADGET_SOURCE_BLOCK), true);
            usePaste = nbt.getBoolean(NBTKeys.GADGET_USE_PASTE);
        }
    }

}

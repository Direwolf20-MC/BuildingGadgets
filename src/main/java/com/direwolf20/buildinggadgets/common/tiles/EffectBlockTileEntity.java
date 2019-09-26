package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

import javax.annotation.Nullable;

public class EffectBlockTileEntity extends TileEntity implements ITickableTileEntity {

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
        super(OurBlocks.OurTileEntities.EFFECT_BLOCK_TYPE);
    }

    public void initializeData(BlockState curState, @Nullable TileEntity te, BlockData replacementBlock, Mode mode, boolean usePaste) {
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
        //world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 0);
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks >= getLifespan()) {
            complete();
        }
    }

    private void complete() {
        if (world.isRemote || mode == null || renderedBlock == null)
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
    public SUpdateTileEntityPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        read(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt(NBTKeys.GADGET_TICKS, ticks);
        compound.putInt(NBTKeys.GADGET_MODE, mode.ordinal());
        compound.put(NBTKeys.GADGET_REPLACEMENT_BLOCK, renderedBlock.serialize(true));
        compound.put(NBTKeys.GADGET_SOURCE_BLOCK, sourceBlock.serialize(true));
        compound.putBoolean(NBTKeys.GADGET_USE_PASTE, usePaste);

        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        ticks = compound.getInt(NBTKeys.GADGET_TICKS);
        mode = Mode.VALUES[compound.getInt(NBTKeys.GADGET_MODE)];
        renderedBlock = BlockData.tryDeserialize(compound.getCompound(NBTKeys.GADGET_REPLACEMENT_BLOCK), true);
        sourceBlock = BlockData.tryDeserialize(compound.getCompound(NBTKeys.GADGET_SOURCE_BLOCK), true);
        usePaste = compound.getBoolean(NBTKeys.GADGET_USE_PASTE);
    }
}

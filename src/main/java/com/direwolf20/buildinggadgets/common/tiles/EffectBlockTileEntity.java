package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks.BGTileEntities;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
        super(BGTileEntities.EFFECT_BLOCK_TYPE);
    }

    public void initializeData(World world, BlockData replacementBlock, Mode mode, boolean usePaste) {
        // Minecraft will reuse a tile entity object at a location where the block got removed, but the modification is still buffered, and the block got restored again
        // If we don't reset this here, the 2nd phase of REPLACE will simply finish immediately because the tile entity object is reused
        this.ticks = 0;
        // Again we don't check if the data has been set or not because there is a chance that this tile object gets reused

        BlockData currentBlock = Registries.TileEntityData.createBlockData(world, pos);
        TileEntity te = world.getTileEntity(pos);
        this.sourceBlock = replacementBlock;

        this.mode = mode;
        this.usePaste = usePaste;

        if (mode == Mode.REPLACE)
            this.renderedBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : currentBlock;
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
        return new SUpdateTileEntityPacket(pos, 0, write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("ticks", ticks);
        compound.putInt("mode", mode.ordinal());
        compound.put("replacement_block", renderedBlock.serialize(true));
        compound.put("source_block", sourceBlock.serialize(true));
        compound.putBoolean("use_paste", usePaste);

        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        ticks = compound.getInt("ticks");
        mode = Mode.VALUES[compound.getInt("mode")];
        renderedBlock = BlockData.deserialize(compound.getCompound("replacement_block"), true);
        sourceBlock = BlockData.deserialize(compound.getCompound("source_block"), true);
        usePaste = compound.getBoolean("use_paste");
    }
}

package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import com.direwolf20.buildinggadgets.api.template.building.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks.BGTileEntities;
import com.direwolf20.buildinggadgets.common.registry.objects.BGEntities;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EffectBlockTileEntity extends TileEntity implements ITickableTileEntity {

    public enum Mode {
        // Serialization and networking based on `ordinal()`, please DO NOT CHANGE THE ORDER of the enums
        PLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                World world = builder.world;
                BlockPos targetPos = builder.getPos();
                BlockData targetBlock = builder.getReplacementBlock();
                if (builder.isUsingPaste()) {
                    world.setBlockState(targetPos, BGBlocks.constructionBlock.getDefaultState());
                    TileEntity te = world.getTileEntity(targetPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(targetBlock, targetBlock);
                    }
                    world.addEntity(new ConstructionBlockEntity(world, targetPos, false));
                } else {
                    world.removeBlock(targetPos, false);
                    targetBlock.placeIn(SimpleBuildContext.builder().build(world), targetPos);
                    BlockPos upPos = targetPos.up();
                    world.getBlockState(targetPos).neighborChanged(world, targetPos, world.getBlockState(upPos).getBlock(), upPos, false);
                }
            }
        },
        REMOVE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                builder.world.removeBlock(builder.getPos(), false);
            }
        },
        REPLACE() {
            @Override
            public void onBuilderRemoved(EffectBlockTileEntity builder) {
                World world = builder.world;
                EffectBlock.spawnEffectBlock(world, builder.getPos(), builder.getSourceBlock(), PLACE, builder.isUsingPaste());
            }
        };

        public static final Mode[] VALUES = values();

        public abstract void onBuilderRemoved(EffectBlockTileEntity builder);
    }

    private BlockData replacementBlock;
    private BlockData sourceBlock;

    private Mode mode = null;
    private boolean usePaste;

    private int ticks;

    public EffectBlockTileEntity() {
        super(BGTileEntities.EFFECT_BLOCK_TYPE);
    }

    public void initializeData(World world, BlockData spawnBlock, Mode mode, boolean usePaste) {
        // We use the field mode as an indicator of initialized or not
        Preconditions.checkState(this.mode == null);

        BlockData currentBlock = Registries.TileEntityData.createBlockData(world, pos);
        TileEntity te = world.getTileEntity(pos);
        sourceBlock = spawnBlock;

        this.mode = mode;
        this.usePaste = usePaste;

        if (mode == EffectBlockTileEntity.Mode.REPLACE)
            replacementBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : currentBlock;
        else
            replacementBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : spawnBlock;

    }

    @Override
    public void tick() {
        ticks++;
        if (ticks >= getLifespan()) {
            complete();
        }
    }

    private void complete() {
        if (world.isRemote || mode == null || replacementBlock == null)
            return;
        mode.onBuilderRemoved(this);
    }

    public BlockData getReplacementBlock() {
        return replacementBlock;
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

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        // TODO figure out the type
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
        compound.put("replacement_block", replacementBlock.serialize(true));
        compound.put("source_block", sourceBlock.serialize(true));
        compound.putBoolean("use_paste", usePaste);

        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        ticks = compound.getInt("ticks");
        mode = Mode.VALUES[compound.getInt("mode")];
        replacementBlock = BlockData.deserialize(compound.getCompound("replacement_block"), true);
        sourceBlock = BlockData.deserialize(compound.getCompound("source_block"), true);
        usePaste = compound.getBoolean("use_paste");
    }
}

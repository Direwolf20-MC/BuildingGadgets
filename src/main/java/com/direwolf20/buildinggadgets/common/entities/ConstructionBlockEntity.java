package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGEntities;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConstructionBlockEntity extends EntityBase {
    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(ConstructionBlockEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> MAKING = EntityDataManager.createKey(ConstructionBlockEntity.class, DataSerializers.BOOLEAN);

    public ConstructionBlockEntity(World world) {
        super(BGEntities.CONSTRUCTION_BLOCK, world);
    }

    public ConstructionBlockEntity(World world, BlockPos spawnPos, boolean makePaste) {
        this(world);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        targetPos = spawnPos;
        setMakingPaste(makePaste);
    }

    @Override
    protected int getMaxLife() {
        return 80;
    }

    @Override
    protected void registerData() {
        dataManager.register(FIXED, BlockPos.ORIGIN);
        dataManager.register(MAKING, false);
    }

    @Override
    protected boolean shouldSetDespawning() {
        if (super.shouldSetDespawning())
            return true;

        if (targetPos == null)
            return false;

        Block block = world.getBlockState(targetPos).getBlock();
        return !(block instanceof ConstructionBlock) && !(block instanceof ConstructionBlockPowder);
    }

    @Override
    protected void onSetDespawning() {
        if (targetPos != null) {
            if (!getMakingPaste()) {
                TileEntity te = world.getTileEntity(targetPos);
                if (te instanceof ConstructionBlockTileEntity) {
                    IBlockState tempState = te.getBlockState();

                    int opacity = tempState.getOpacity(world, targetPos);
                    boolean neighborBrightness = tempState.useNeighborBrightness(world, targetPos);
                    if (opacity > 0 || neighborBrightness) {
                        IBlockState tempSetBlock = te.getBlockState();
                        IBlockState tempActualSetBlock = ((ConstructionBlockTileEntity) te).getActualBlockState();
                        world.setBlockState(targetPos, BGBlocks.constructionBlock.getDefaultState()
                                .with(ConstructionBlock.BRIGHT, opacity == 0)
                                .with(ConstructionBlock.NEIGHBOR_BRIGHTNESS, neighborBrightness));
                        te = world.getTileEntity(targetPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            ((ConstructionBlockTileEntity) te).setBlockState(tempSetBlock, tempActualSetBlock);
                        }
                    }
                }
            } else if (world.getBlockState(targetPos) == BGBlocks.constructionBlockPowder.getDefaultState()) {
                world.setBlockState(targetPos, BGBlocks.constructionBlockDense.getDefaultState());
            }
        }
    }

    public void setMakingPaste(boolean paste) {
        dataManager.set(MAKING, paste);
    }

    public boolean getMakingPaste() {
        return dataManager.get(MAKING);
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        super.readAdditional(compound);
        setMakingPaste(compound.getBoolean(NBTKeys.ENTITY_CONSTRUCTION_MAKING_PASTE));
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        super.writeAdditional(compound);
        compound.setBoolean(NBTKeys.ENTITY_CONSTRUCTION_MAKING_PASTE, getMakingPaste());
    }
}

package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGEntities;
import com.direwolf20.buildinggadgets.common.utils.ref.NBTKeys;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockBuildEntity extends EntityModBase {
    private static final DataParameter<Integer> toolMode = EntityDataManager.<Integer>createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<IBlockState>> SET_BLOCK = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> usePaste = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BOOLEAN);

    private IBlockState setBlock;
    private IBlockState originalSetBlock;
    private IBlockState actualSetBlock;
    private EntityLivingBase spawnedBy;

    private int mode;
    private boolean useConstructionPaste;

    public BlockBuildEntity(World world) {
        super(BGEntities.BUILD_BLOCK, world);
    }

    public BlockBuildEntity(World world, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock, int toolMode, IBlockState actualSpawnBlock, boolean constrPaste) {
        this(world);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        IBlockState currentBlock = world.getBlockState(spawnPos);
        TileEntity te = world.getTileEntity(spawnPos);

        setPos = spawnPos;
        setBlock = te instanceof ConstructionBlockTileEntity ? te.getBlockState() : spawnBlock;
        originalSetBlock = spawnBlock;

        setSetBlock(setBlock);

        if (toolMode == 3) {
            setBlock = te instanceof ConstructionBlockTileEntity ? te.getBlockState() : currentBlock;
            setSetBlock(setBlock);
        }

        mode = toolMode;
        setToolMode(toolMode);

        spawnedBy = player;
        actualSetBlock = actualSpawnBlock;
        world.setBlockState(spawnPos, BGBlocks.effectBlock.getDefaultState());

        setUsingConstructionPaste(constrPaste);
    }

    @Override
    protected int getMaxLife() {
        return 20;
    }

    public int getToolMode() {
        return dataManager.get(toolMode);
    }

    public void setToolMode(int mode) {
        dataManager.set(toolMode, mode);
    }

    @Nullable
    public IBlockState getSetBlock() {
        return dataManager.get(SET_BLOCK).orElse(null);
    }

    public void setSetBlock(@Nullable IBlockState state) {
        dataManager.set(SET_BLOCK, Optional.ofNullable(state));
    }

    public void setUsingConstructionPaste(Boolean paste) {
        dataManager.set(usePaste, paste);
    }

    public boolean getUsingConstructionPaste() {
        return dataManager.get(usePaste);
    }

    @Override
    protected void registerData() {
        dataManager.register(FIXED, BlockPos.ORIGIN);
        dataManager.register(toolMode, 1);
        dataManager.register(SET_BLOCK, Optional.empty());
        dataManager.register(usePaste, useConstructionPaste);
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        super.readAdditional(compound);
        setBlock = NBTUtil.readBlockState(compound.getCompound(NBTKeys.ENTITY_BUILD_SET_BLOCK));
        actualSetBlock = NBTUtil.readBlockState(compound.getCompound(NBTKeys.ENTITY_BUILD_SET_BLOCK));
        originalSetBlock = NBTUtil.readBlockState(compound.getCompound(NBTKeys.ENTITY_BUILD_ORIGINAL_BLOCK));
        mode = compound.getInt(NBTKeys.GADGET_MODE);
        useConstructionPaste = compound.getBoolean(NBTKeys.ENTITY_BUILD_USE_PASTE);
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        super.writeAdditional(compound);

        NBTTagCompound blockStateTag = NBTUtil.writeBlockState(setBlock);
        compound.setTag(NBTKeys.ENTITY_BUILD_SET_BLOCK, blockStateTag);

        NBTTagCompound actualBlockStateTag = NBTUtil.writeBlockState(actualSetBlock);
        compound.setTag(NBTKeys.ENTITY_BUILD_SET_BLOCK_ACTUAL, actualBlockStateTag);

        blockStateTag = NBTUtil.writeBlockState(originalSetBlock);

        compound.setTag(NBTKeys.ENTITY_BUILD_ORIGINAL_BLOCK, blockStateTag);
        compound.setInt(NBTKeys.GADGET_MODE, mode);
        compound.setBoolean(NBTKeys.ENTITY_BUILD_USE_PASTE, useConstructionPaste);
    }

    @Override
    protected void onSetDespawning() {
        if (setPos != null && setBlock != null && (getToolMode() == 1)) {
            if (getUsingConstructionPaste()) {
                world.setBlockState(setPos, BGBlocks.constructionBlock.getDefaultState());
                TileEntity te = world.getTileEntity(setPos);
                if (te instanceof ConstructionBlockTileEntity) {
                    ((ConstructionBlockTileEntity) te).setBlockState(setBlock, actualSetBlock);
                }
                world.spawnEntity(new ConstructionBlockEntity(world, setPos, false));
            } else {
                world.setBlockState(setPos, setBlock);
                world.getBlockState(setPos).neighborChanged(world, setPos, world.getBlockState(setPos.up()).getBlock(), setPos.up());
            }
        } else if (setPos != null && setBlock != null && getToolMode() == 2) {
            world.setBlockState(setPos, Blocks.AIR.getDefaultState());
        } else if (setPos != null && setBlock != null && getToolMode() == 3) {
            world.spawnEntity(new BlockBuildEntity(world, setPos, spawnedBy, originalSetBlock, 1, actualSetBlock, getUsingConstructionPaste()));
        }
    }
}

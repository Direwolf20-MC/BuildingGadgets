package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGEntities;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
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

public class BlockBuildEntity extends EntityBase {

    public static final int MODE_PLACE = 1;
    public static final int MODE_REMOVE = 2;
    public static final int MODE_REPLACE = 3;

    private static final DataParameter<Integer> TOOL_MODE = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<IBlockState>> SET_BLOCK = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<Boolean> USE_PASTE = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BOOLEAN);

    private IBlockState setBlock;
    private IBlockState originalSetBlock;
    private EntityLivingBase spawnedBy;

    private int mode;
    private boolean useConstructionPaste;

    public BlockBuildEntity(World world) {
        super(BGEntities.BUILD_BLOCK, world);
    }

    public BlockBuildEntity(World world, BlockPos spawnPos, EntityLivingBase player, IBlockState spawnBlock, int toolMode, boolean usePaste) {
        this(world);
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        IBlockState currentBlock = world.getBlockState(spawnPos);
        TileEntity te = world.getTileEntity(spawnPos);
        targetPos = spawnPos;
        originalSetBlock = spawnBlock;

        if (toolMode == MODE_REPLACE)
            setBlock = te instanceof ConstructionBlockTileEntity ? te.getBlockState() : currentBlock;
        else
            setBlock = te instanceof ConstructionBlockTileEntity ? te.getBlockState() : spawnBlock;
        setSetBlock(setBlock);

        mode = toolMode;
        setToolMode(toolMode);

        spawnedBy = player;
        world.setBlockState(spawnPos, BGBlocks.effectBlock.getDefaultState());

        setUsingPaste(usePaste);
    }

    @Override
    protected int getMaxLife() {
        return 20;
    }

    public int getToolMode() {
        return dataManager.get(TOOL_MODE);
    }

    public void setToolMode(int mode) {
        dataManager.set(TOOL_MODE, mode);
    }

    @Nullable
    public IBlockState getSetBlock() {
        return dataManager.get(SET_BLOCK).orElse(null);
    }

    public void setSetBlock(@Nullable IBlockState state) {
        dataManager.set(SET_BLOCK, Optional.ofNullable(state));
    }

    public void setUsingPaste(Boolean paste) {
        dataManager.set(USE_PASTE, paste);
    }

    public boolean isUsingPaste() {
        return dataManager.get(USE_PASTE);
    }

    @Override
    protected void registerData() {
        dataManager.register(TOOL_MODE, 1);
        dataManager.register(SET_BLOCK, Optional.empty());
        dataManager.register(USE_PASTE, useConstructionPaste);
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        super.readAdditional(compound);
        setBlock = NBTUtil.readBlockState(compound.getCompound(NBTKeys.ENTITY_BUILD_SET_BLOCK));
        originalSetBlock = NBTUtil.readBlockState(compound.getCompound(NBTKeys.ENTITY_BUILD_ORIGINAL_BLOCK));
        mode = compound.getInt(NBTKeys.GADGET_MODE);
        useConstructionPaste = compound.getBoolean(NBTKeys.ENTITY_BUILD_USE_PASTE);
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        super.writeAdditional(compound);

        NBTTagCompound blockStateTag = NBTUtil.writeBlockState(setBlock);
        compound.setTag(NBTKeys.ENTITY_BUILD_SET_BLOCK, blockStateTag);

        blockStateTag = NBTUtil.writeBlockState(originalSetBlock);

        compound.setTag(NBTKeys.ENTITY_BUILD_ORIGINAL_BLOCK, blockStateTag);
        compound.setInt(NBTKeys.GADGET_MODE, mode);
        compound.setBoolean(NBTKeys.ENTITY_BUILD_USE_PASTE, useConstructionPaste);
    }

    @Override
    protected void onSetDespawning() {
        if (world.isRemote || targetPos == null || setBlock == null)
            return;

        switch (getToolMode()) {
            case MODE_PLACE:
                if (isUsingPaste()) {
                    world.setBlockState(targetPos, BGBlocks.constructionBlock.getDefaultState());
                    TileEntity te = world.getTileEntity(targetPos);
                    if (te instanceof ConstructionBlockTileEntity) {
                        ((ConstructionBlockTileEntity) te).setBlockState(setBlock, setBlock);
                    }
                    world.spawnEntity(new ConstructionBlockEntity(world, targetPos, false));
                } else {
                    world.setBlockState(targetPos, setBlock);
                    BlockPos upPos = targetPos.up();
                    world.getBlockState(targetPos).neighborChanged(world, targetPos, world.getBlockState(upPos).getBlock(), upPos);
                }
                break;
            case MODE_REMOVE:
                world.setBlockState(targetPos, Blocks.AIR.getDefaultState());
                break;
            case MODE_REPLACE:
                world.spawnEntity(new BlockBuildEntity(world, targetPos, spawnedBy, originalSetBlock, MODE_PLACE, isUsingPaste()));
                break;
        }
    }

}

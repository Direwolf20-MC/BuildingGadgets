package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockBuildEntity extends EntityBase {

    private static final DataParameter<Integer> MODE = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<BlockState>> SET_BLOCK = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<Boolean> USE_PASTE = EntityDataManager.createKey(BlockBuildEntity.class, DataSerializers.BOOLEAN);

    private BlockData setBlock;
    private BlockData originalSetBlock;

    private EffectBlock.Mode mode;
    private boolean useConstructionPaste;

    public BlockBuildEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    // public BlockBuildEntity(World world, BlockPos spawnPos, BlockData spawnBlock, EffectBlockTileEntity.Mode mode, boolean usePaste) {
    //     this(BGEntities.BUILD_BLOCK, world);
    //     setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    //
    //     BlockData currentBlock = Registries.TileEntityData.createBlockData(world, spawnPos);
    //     TileEntity te = world.getTileEntity(spawnPos);
    //     targetPos = spawnPos;
    //     originalSetBlock = spawnBlock;
    //
    //     if (mode == EffectBlockTileEntity.Mode.REPLACE)
    //         setBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : currentBlock;
    //     else
    //         setBlock = te instanceof ConstructionBlockTileEntity ? ((ConstructionBlockTileEntity) te).getConstructionBlockData() : spawnBlock;
    //     setSetBlock(setBlock);
    //
    //     this.mode = mode;
    //     setToolMode(mode);
    //
    //     world.setBlockState(spawnPos, BGBlocks.effectBlock.getDefaultState());
    //
    //     setUsingPaste(usePaste);
    // }

    @Override
    protected int getMaxLife() {
        return 20;
    }

    public EffectBlock.Mode getToolMode() {
        return EffectBlock.Mode.VALUES[dataManager.get(MODE)];
    }

    public void setToolMode(EffectBlock.Mode mode) {
        dataManager.set(MODE, mode.ordinal());
    }

    @Nullable
    public BlockState getSetBlock() {
        return dataManager.get(SET_BLOCK).orElse(null);
    }

    public void setSetBlock(@Nullable BlockData data) {
        dataManager.set(SET_BLOCK, Optional.ofNullable(data != null ? data.getState() : null));
    }

    public void setUsingPaste(boolean paste) {
        dataManager.set(USE_PASTE, paste);
    }

    public boolean isUsingPaste() {
        return dataManager.get(USE_PASTE);
    }

    @Override
    protected void registerData() {
        dataManager.register(MODE, EffectBlock.Mode.PLACE.ordinal());
        dataManager.register(SET_BLOCK, Optional.empty());
        dataManager.register(USE_PASTE, useConstructionPaste);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        setBlock = BlockData.deserialize(compound.getCompound(NBTKeys.ENTITY_BUILD_SET_BLOCK), true);
        originalSetBlock = BlockData.deserialize(compound.getCompound(NBTKeys.ENTITY_BUILD_ORIGINAL_BLOCK), true);
        mode = EffectBlock.Mode.VALUES[compound.getInt(NBTKeys.GADGET_MODE)];
        useConstructionPaste = compound.getBoolean(NBTKeys.ENTITY_BUILD_USE_PASTE);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        CompoundNBT blockStateTag = setBlock.serialize(true);
        compound.put(NBTKeys.ENTITY_BUILD_SET_BLOCK, blockStateTag);

        blockStateTag = originalSetBlock.serialize(true);

        compound.put(NBTKeys.ENTITY_BUILD_ORIGINAL_BLOCK, blockStateTag);
        compound.putInt(NBTKeys.GADGET_MODE, mode.ordinal());
        compound.putBoolean(NBTKeys.ENTITY_BUILD_USE_PASTE, useConstructionPaste);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onSetDespawning() {
        if (world.isRemote || targetPos == null || setBlock == null)
            return;
        // mode.onBuilderRemoved(this);
    }

}

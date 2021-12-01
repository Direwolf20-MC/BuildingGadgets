package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

public class ConstructionBlockEntity extends EntityBase {
    @ObjectHolder(Reference.EntityReference.CONSTRUCTION_BLOCK_ENTITY)
    public static EntityType<ConstructionBlockEntity> TYPE;

    private static final EntityDataAccessor<BlockPos> FIXED = SynchedEntityData.defineId(ConstructionBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> MAKING = SynchedEntityData.defineId(ConstructionBlockEntity.class, EntityDataSerializers.BOOLEAN);

    public ConstructionBlockEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    public ConstructionBlockEntity(Level world, BlockPos spawnPos, boolean makePaste) {
        this(TYPE, world);
        
        setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        targetPos = spawnPos;
        setMakingPaste(makePaste);
    }

    @Override
    protected int getMaxLife() {
        return 80;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FIXED, BlockPos.ZERO);
        entityData.define(MAKING, false);
    }

    @Override
    protected boolean shouldSetDespawning() {
        if (super.shouldSetDespawning())
            return true;

        if (targetPos == null)
            return false;

        Block block = level.getBlockState(targetPos).getBlock();
        return !(block instanceof ConstructionBlock) && !(block instanceof ConstructionBlockPowder);
    }

    @Override
    protected void onSetDespawning() {
        if (targetPos != null) {
            if (!getMakingPaste()) {
                BlockEntity te = level.getBlockEntity(targetPos);
                if (te instanceof ConstructionBlockTileEntity) {
                    BlockData tempState = ((ConstructionBlockTileEntity) te).getConstructionBlockData();

                    boolean opaque = tempState.getState().isSolidRender(level, targetPos);
                    boolean neighborBrightness = false;//tempState.useNeighbourBrightness(world, targetPos); //TODO find replacement
                    //IBakedModel model;
                    //model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(tempState.getState());
                    //boolean ambient = model.isAmbientOcclusion();
                    boolean ambient = false; //TODO Find a better way to get the proper ambient Occlusion value. This is client side only so can't be done here.
                    if (opaque || neighborBrightness || ! ambient) {
                        BlockData tempSetBlock = ((ConstructionBlockTileEntity) te).getConstructionBlockData();
                        level.setBlockAndUpdate(targetPos, OurBlocks.CONSTRUCTION_BLOCK.get().defaultBlockState()
                                .setValue(ConstructionBlock.BRIGHT, ! opaque)
                                .setValue(ConstructionBlock.NEIGHBOR_BRIGHTNESS, neighborBrightness)
                                .setValue(ConstructionBlock.AMBIENT_OCCLUSION, ambient));
                        te = level.getBlockEntity(targetPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            ((ConstructionBlockTileEntity) te).setBlockState(tempSetBlock);
                        }
                    }
                }
            } else if (level.getBlockState(targetPos) == OurBlocks.CONSTRUCTION_POWDER_BLOCK.get().defaultBlockState()) {
                level.setBlockAndUpdate(targetPos, OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState());
            }
        }
    }

    public void setMakingPaste(boolean paste) {
        entityData.set(MAKING, paste);
    }

    public boolean getMakingPaste() {
        return entityData.get(MAKING);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setMakingPaste(compound.getBoolean(NBTKeys.ENTITY_CONSTRUCTION_MAKING_PASTE));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean(NBTKeys.ENTITY_CONSTRUCTION_MAKING_PASTE, getMakingPaste());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

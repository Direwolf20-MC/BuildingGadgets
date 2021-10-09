package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class EntityBase extends Entity {
    private int despawning = -1;
    protected BlockPos targetPos;

    public EntityBase(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    protected abstract int getMaxLife();

    protected abstract void onSetDespawning();

    @Override
    public void baseTick() {
        super.baseTick();
        if (despawning == -1 && shouldSetDespawning()) {
            despawning = 0;
            onSetDespawning();
        } else if (despawning != -1 && ++despawning > 1)
            remove(RemovalReason.DISCARDED);
    }

    protected boolean shouldSetDespawning() {
        return tickCount > getMaxLife();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        despawning = compound.getInt(NBTKeys.ENTITY_DESPAWNING);
        tickCount = compound.getInt(NBTKeys.ENTITY_TICKS_EXISTED);
        targetPos = NbtUtils.readBlockPos(compound.getCompound(NBTKeys.ENTITY_SET_POS));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(NBTKeys.ENTITY_DESPAWNING, despawning);
        compound.putInt(NBTKeys.ENTITY_TICKS_EXISTED, tickCount);
        compound.put(NBTKeys.ENTITY_SET_POS, NbtUtils.writeBlockPos(targetPos));
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
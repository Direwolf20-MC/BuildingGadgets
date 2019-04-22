package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class EntityBase extends Entity {
    private int despawning = -1;
    protected BlockPos targetPos;

    public EntityBase(EntityType<?> entityType, World world) {
        super(entityType, world);
        setSize(0.1F, 0.1F);
    }

    protected abstract int getMaxLife();

    protected abstract void onSetDespawning();

    protected int getTicksExisted() {
        return ticksExisted;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (despawning == -1 && shouldSetDespawning()) {
            despawning = 0;
            onSetDespawning();
        } else if (despawning != -1 && ++despawning > 1)
            remove();
    }

    protected boolean shouldSetDespawning() {
        return ticksExisted > getMaxLife();
    }

    @Override
    protected void readAdditional(NBTTagCompound compound) {
        despawning = compound.getInt(NBTKeys.ENTITY_DESPAWNING);
        ticksExisted = compound.getInt(NBTKeys.ENTITY_TICKS_EXISTED);
        targetPos = NBTUtil.readBlockPos(compound.getCompound(NBTKeys.ENTITY_SET_POS));
    }

    @Override
    protected void writeAdditional(NBTTagCompound compound) {
        compound.setInt(NBTKeys.ENTITY_DESPAWNING, despawning);
        compound.setInt(NBTKeys.ENTITY_TICKS_EXISTED, ticksExisted);
        compound.setTag(NBTKeys.ENTITY_SET_POS, NBTUtil.writeBlockPos(targetPos));
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }
}
package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

public final class ItemEnergyForge implements IEnergyStorage {
    private static final String NBT_ENERGY = "Energy";
    private final ItemStack stack;
    private final IntSupplier capacitySupplier;
    private int energy;

    public ItemEnergyForge(ItemStack stack, IntSupplier capacity) {
        this.capacitySupplier = capacity;
        this.stack = stack;
        this.energy = 0;
        updateEnergy();
    }

    @Override
    public int getEnergyStored() {
        updateEnergy();
        return getEnergyStoredCache();
    }

    @Override
    public int getMaxEnergyStored() {
        return capacitySupplier.getAsInt();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        updateEnergy();
        int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStoredCache(), maxReceive);
        if (!simulate) {
            energy += energyReceived;
            writeEnergy();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        updateEnergy();
        int energyExtracted = Math.min(getEnergyStoredCache(), maxExtract);
        if (!simulate) {
            energy -= energyExtracted;
            writeEnergy();
        }
        return energyExtracted;
    }

    /**
     * Returns if this storage can have energy extracted.
     * If this is false, then any calls to extractEnergy will return 0.
     */
    @Override
    public boolean canExtract() {
        return true;
    }

    /**
     * Used to determine if this storage can receive energy.
     * If this is false, then any calls to receiveEnergy will return 0.
     */
    @Override
    public boolean canReceive() {
        return true;
    }

    private int getEnergyStoredCache() {
        return energy;
    }

    private void writeEnergy() {
        NBTTagCompound nbt = GadgetUtils.enforceHasTag(stack);
        nbt.setInt(NBT_ENERGY,getEnergyStoredCache());
    }

    private void updateEnergy() {
        NBTTagCompound nbt = GadgetUtils.enforceHasTag(stack);
        if (nbt.hasKey(NBT_ENERGY))
            this.energy = nbt.getInt(NBT_ENERGY) ;
        updateMaxEnergy();
    }

    private void updateMaxEnergy() {
        this.energy = Math.min(getMaxEnergyStored(),energy);
    }
}

package com.direwolf20.buildinggadgets.common.capability;

import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

public abstract class ConfigEnergyStorage implements IEnergyStorage {
    private final IntSupplier capacitySupplier;
    private int energy;

    public ConfigEnergyStorage(IntSupplier capacity) {
        this.capacitySupplier = capacity;
        this.energy = 0;
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
        int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), maxReceive);
        if (! simulate) {
            energy += energyReceived;
            writeEnergy();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(getEnergyStored(), maxExtract);
        if (! simulate) {
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

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    protected int getEnergyStoredCache() {
        return energy;
    }

    protected abstract void writeEnergy();

    protected abstract void updateEnergy();

    protected void updateMaxEnergy() {
        this.energy = Math.min(getMaxEnergyStored(), energy);
    }
}

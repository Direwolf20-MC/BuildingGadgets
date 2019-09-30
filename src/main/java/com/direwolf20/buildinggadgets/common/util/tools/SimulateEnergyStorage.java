package com.direwolf20.buildinggadgets.common.util.tools;

import net.minecraftforge.energy.IEnergyStorage;

public final class SimulateEnergyStorage implements IEnergyStorage {
    private final IEnergyStorage other;
    private int energyChanged;

    public SimulateEnergyStorage(IEnergyStorage other) {
        this.other = other;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = other.receiveEnergy(maxReceive + energyChanged, true);
        energyChanged += received;
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = other.extractEnergy(maxExtract - energyChanged, true);
        energyChanged -= extracted;
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return Math.max(other.getEnergyStored() + energyChanged, 0);
    }

    @Override
    public int getMaxEnergyStored() {
        return other.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return other.canExtract();
    }

    @Override
    public boolean canReceive() {
        return other.canReceive();
    }
}

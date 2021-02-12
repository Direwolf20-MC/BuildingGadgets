package com.direwolf20.buildinggadgets.common.capability.energy;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

@Tainted(reason = "This shouldn't have have 3 layers")
public abstract class ConfigEnergyStorage implements IEnergyStorage {
    private final IntSupplier capacitySupplier;
    private final IntSupplier extractSupplier;
    private final IntSupplier receiveSupplier;
    private int energy;

    public ConfigEnergyStorage(IntSupplier capacitySupplier, IntSupplier extractSupplier, IntSupplier receiveSupplier) {
        this.capacitySupplier = capacitySupplier;
        this.extractSupplier = extractSupplier;
        this.receiveSupplier = receiveSupplier;
        this.energy = 0;
    }

    public ConfigEnergyStorage(IntSupplier capacitySupplier, IntSupplier transfer) {
        this(capacitySupplier, transfer, transfer);
    }

    public ConfigEnergyStorage(IntSupplier capacity) {
        this(capacity, capacity);
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
        if (maxReceive < 0)
            return 0;
        int energyReceived = evaluateEnergyReceived(maxReceive, simulate);
        if (! simulate) {
            setEnergy(energyReceived + getEnergyStored());
            writeEnergy();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract < 0)
            return 0;
        int energyExtracted = evaluateEnergyExtracted(maxExtract, simulate);
        if (! simulate) {
            setEnergy(getEnergyStored() - energyExtracted);
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
        return extractSupplier.getAsInt() > 0;
    }

    /**
     * Used to determine if this storage can receive energy.
     * If this is false, then any calls to receiveEnergy will return 0.
     */
    @Override
    public boolean canReceive() {
        return receiveSupplier.getAsInt() > 0;
    }

    protected IntSupplier getExtractSupplier() {
        return extractSupplier;
    }

    protected IntSupplier getReceiveSupplier() {
        return receiveSupplier;
    }

    protected int evaluateEnergyExtracted(int maxExtract, boolean simulate) {
        return Math.min(getEnergyStored(), Math.min(maxExtract, getExtractSupplier().getAsInt()));
    }

    protected int evaluateEnergyReceived(int maxReceive, boolean simulate) {
        return Math.min(getMaxEnergyStored() - getEnergyStored(), Math.min(maxReceive, getReceiveSupplier().getAsInt()));
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

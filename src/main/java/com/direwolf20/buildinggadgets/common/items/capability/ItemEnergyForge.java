package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.ref.NBTKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

public final class ItemEnergyForge implements IEnergyStorage {
    private final ItemStack stack;
    private final IntSupplier capacitySupplier;
    private int energy;

    public ItemEnergyForge(ItemStack stack, IntSupplier capacity) {
        this.capacitySupplier = capacity;
        this.stack = stack;
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
        if (!simulate) {
            energy += energyReceived;
            writeEnergy();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(getEnergyStored(), maxExtract);
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
        nbt.setInt(NBTKeys.ENERGY,getEnergyStoredCache());
    }

    private void updateEnergy() {
        NBTTagCompound nbt = GadgetUtils.enforceHasTag(stack);
        if (nbt.hasKey(NBTKeys.ENERGY))
            this.energy = nbt.getInt(NBTKeys.ENERGY) ;
        updateMaxEnergy();
    }

    private void updateMaxEnergy() {
        this.energy = Math.min(getMaxEnergyStored(),energy);
    }
}

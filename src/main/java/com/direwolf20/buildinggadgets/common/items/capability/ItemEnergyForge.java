package com.direwolf20.buildinggadgets.common.items.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

import java.util.function.IntSupplier;

public class ItemEnergyForge extends EnergyStorage {

    private static final String NBT_ENERGY = "Energy";

    private final ItemStack stack;
    private final IntSupplier capacitySupplier;

    public ItemEnergyForge(ItemStack stack, IntSupplier capacity) {
        super(capacity.getAsInt(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.capacitySupplier = capacity;
        this.stack = stack;
        updateEnergy();
    }

    @Override
    public int getEnergyStored() {
        updateEnergy();
        return super.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        updateMaxEnergy();
        return super.getMaxEnergyStored();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        updateEnergy();
        return addEnergy(super.receiveEnergy(maxReceive, simulate), simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        updateEnergy();
        return removeEnergy(super.extractEnergy(maxExtract, simulate), simulate);
    }

    //TODO De-Dire this code.
    private int addEnergy(int amount, boolean simulate) {
        if (amount > 0 && !simulate) {
            NBTTagCompound nbt = stack.getTag();
            if (nbt == null) {
                stack.setTag(nbt = new NBTTagCompound());
            }

            nbt.setInt(NBT_ENERGY, getEnergyStored() + amount);
        }
        return amount;
    }

    private int removeEnergy(int amount, boolean simulate) {
        if (amount > 0 && !simulate) {
            NBTTagCompound nbt = stack.getTag();
            if (nbt == null) {
                stack.setTag(nbt = new NBTTagCompound());
            }

            nbt.setInt(NBT_ENERGY, getEnergyStored() - amount);
        }
        return amount;
    }

    private void updateEnergy() {
        NBTTagCompound nbt = stack.getTag();
        this.energy = nbt != null && nbt.hasKey(NBT_ENERGY) ? nbt.getInt(NBT_ENERGY) : 0;
        updateMaxEnergy();
    }

    private void updateMaxEnergy() {
        this.capacity = capacitySupplier.getAsInt();
        this.energy = Math.min(capacity,energy);
    }
}

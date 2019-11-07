package com.direwolf20.buildinggadgets.common.items.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

import java.util.function.IntSupplier;

public class ItemEnergyForge extends EnergyStorage {

    private static final String NBT_ENERGY = "Energy";

    private ItemStack stack;
    private IntSupplier maxEnergyProvider;

    public ItemEnergyForge(ItemStack stack, IntSupplier maxEnergyProvider) {
        super(maxEnergyProvider.getAsInt(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.maxEnergyProvider = maxEnergyProvider;
        this.stack = stack;
        NBTTagCompound nbt = stack.getTagCompound();
        this.energy = nbt != null && nbt.hasKey(NBT_ENERGY) ? nbt.getInteger(NBT_ENERGY) : 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        updateMaxEnergy();
        return changeEnergy(super.receiveEnergy(maxReceive, simulate), simulate);
    }

    /**
     * Do not use internally as this method has been replaced by {@link #extractPower(int, boolean)}
     * to stop other mods using our gadgets as batteries.
     */
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        System.out.println("EXTRACT");
        return 0;
    }

    public int extractPower(int maxExtract, boolean simulate) {
        System.out.println("Safe extract");

        updateMaxEnergy();
        return changeEnergy(super.extractEnergy(maxExtract, simulate), simulate);
    }

    @Override
    public int getMaxEnergyStored() {
        updateMaxEnergy();
        return super.getMaxEnergyStored();
    }

    @Override
    public int getEnergyStored() {
        updateMaxEnergy();
        return super.getEnergyStored();
    }

    private void updateMaxEnergy() {
        this.capacity = maxEnergyProvider.getAsInt();
        this.energy = Math.min(capacity, this.energy);
    }

    private int changeEnergy(int amount, boolean simulate) {
        if (amount > 0 && !simulate) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                stack.setTagCompound(nbt = new NBTTagCompound());
            }

            nbt.setInteger(NBT_ENERGY, getEnergyStored());
        }
        return amount;
    }

}

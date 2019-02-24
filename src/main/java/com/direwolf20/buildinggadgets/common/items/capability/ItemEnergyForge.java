package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
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

    public int getEnergyStoredNoUpdate() {
        updateMaxEnergy();
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
        return updateEnergy(super.receiveEnergy(maxReceive, simulate), simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        updateEnergy();
        return updateEnergy(-super.extractEnergy(maxExtract, simulate), simulate);
    }

    private int updateEnergy(int amount, boolean simulate) {
        if (amount > 0 && !simulate) {
            NBTTagCompound nbt = GadgetUtils.enforceHasTag(stack);
            nbt.setInt(NBT_ENERGY, getEnergyStoredNoUpdate());
        }
        return amount;
    }

    private void updateEnergy() {
        NBTTagCompound nbt = GadgetUtils.enforceHasTag(stack);
        if (nbt.hasKey(NBT_ENERGY))
            this.energy = nbt.getInt(NBT_ENERGY) ;
        updateMaxEnergy();
    }

    private void updateMaxEnergy() {
        this.capacity = capacitySupplier.getAsInt();
        this.energy = Math.min(capacity,energy);
    }
}

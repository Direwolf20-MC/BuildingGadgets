package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.IntSupplier;

public final class ItemEnergyForge extends ConfigEnergyStorage {
    private final ItemStack stack;

    public ItemEnergyForge(ItemStack stack, IntSupplier capacity) {
        super(capacity);
        this.stack = stack;
    }

    protected void writeEnergy() {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        nbt.putInt(NBTKeys.ENERGY, getEnergyStoredCache());
    }

    protected void updateEnergy() {
        CompoundNBT nbt = GadgetUtils.enforceHasTag(stack);
        if (nbt.contains(NBTKeys.ENERGY))
            setEnergy(nbt.getInt(NBTKeys.ENERGY));
        updateMaxEnergy();
    }
}

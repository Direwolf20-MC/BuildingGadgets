package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntSupplier;

public class CapabilityProviderEnergy implements ICapabilityProvider {

    private ItemStack stack;
    private IntSupplier energyCapacity;

    public CapabilityProviderEnergy(ItemStack stack, IntSupplier energyCapacity) {
        this.stack = stack;
        this.energyCapacity = energyCapacity;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && SyncedConfig.poweredByFE;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && SyncedConfig.poweredByFE ? CapabilityEnergy.ENERGY.cast(new ItemEnergyForge(stack, energyCapacity)) : null;
    }

    @Nonnull
    public static IEnergyStorage getCap(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energy == null)
            throw new IllegalArgumentException("CapabilityEnergy could not be retrieved for " + GadgetUtils.getStackErrorSuffix(stack));

        return energy;
    }

}

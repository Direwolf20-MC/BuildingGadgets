package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.OptionalCapabilityInstance;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderEnergy implements ICapabilityProvider {
    private ItemStack stack;
    private int energyCapacity;

    private final OptionalCapabilityInstance<ItemEnergyForge> energyCapability = OptionalCapabilityInstance.of(() -> new ItemEnergyForge(stack, energyCapacity));

    public CapabilityProviderEnergy(ItemStack stack, int energyCapacity) {
        this.stack = stack;
        this.energyCapacity = energyCapacity;
    }

    // @todo: reimplement @since 1.13.x removed as of 1.13?
//    @Override
//    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
//        return capability == CapabilityEnergy.ENERGY;
//    }


    @Nonnull
    @Override
    public <T> OptionalCapabilityInstance<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if( cap == CapabilityEnergy.ENERGY ) {
            return energyCapability.cast();
        }

        return OptionalCapabilityInstance.empty();
    }

    @Nonnull
    public static OptionalCapabilityInstance<IEnergyStorage> getCap(ItemStack stack) {
        OptionalCapabilityInstance<IEnergyStorage> energy = stack.getCapability(CapabilityEnergy.ENERGY);
        if (!energy.isPresent())
            throw new IllegalArgumentException("CapabilityEnergy could not be retrieved for " + GadgetUtils.getStackErrorSuffix(stack));

        return energy;
    }
}

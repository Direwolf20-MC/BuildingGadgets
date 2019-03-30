package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.building.CapabilityBlockProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderBlockProvider implements ICapabilityProvider {

    private final ItemStack stack;

    public CapabilityProviderBlockProvider(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityBlockProvider.BLOCK_PROVIDER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityBlockProvider.BLOCK_PROVIDER)
            return CapabilityBlockProvider.BLOCK_PROVIDER.cast(new LinkedBlockProvider(stack));
        return null;
    }

}

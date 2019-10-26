package com.direwolf20.buildinggadgets.common.capability.provider;

import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.capability.CapabilityBlockProvider;
import com.direwolf20.buildinggadgets.common.capability.LinkedBlockProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderBlockProvider implements ICapabilityProvider {

    private final LinkedBlockProvider provider;
    private final LazyOptional<IBlockProvider> providerCap;

    public CapabilityProviderBlockProvider(ItemStack stack) {
        this.provider = new LinkedBlockProvider(stack);
        providerCap = LazyOptional.of(() -> provider);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityBlockProvider.BLOCK_PROVIDER)
            return providerCap.cast();
        return LazyOptional.empty();
    }
}

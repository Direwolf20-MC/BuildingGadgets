package com.direwolf20.buildinggadgets.common.capability;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class WrappingCapabilityProvider implements ICapabilityProvider {
    private List<ICapabilityProvider> subProviders;

    public WrappingCapabilityProvider(List<ICapabilityProvider> subProviders) {
        this.subProviders = ImmutableList.copyOf(subProviders);
    }

    public WrappingCapabilityProvider(ICapabilityProvider... providers) {
        this(Stream.of(providers).filter(Objects::nonNull).collect(ImmutableList.toImmutableList()));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider provider : subProviders) {
            if (provider.hasCapability(capability, facing)) return true;
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider provider : subProviders) {
            T obj = provider.getCapability(capability, facing);
            if (obj != null) return obj;
        }
        return null;
    }
}

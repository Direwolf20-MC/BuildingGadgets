package com.direwolf20.buildinggadgets.common.items.capability;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultiCapabilityProvider implements ICapabilityProvider {

    private final ImmutableList<ICapabilityProvider> childProviders;

    public MultiCapabilityProvider(ICapabilityProvider... childProviders) {
        this(ImmutableList.copyOf(childProviders));
    }

    public MultiCapabilityProvider(ImmutableList<ICapabilityProvider> childProviders) {
        this.childProviders = childProviders;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider childProvider : childProviders) {
            if (childProvider.hasCapability(capability, facing))
                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider childProvider : childProviders) {
            T handle = childProvider.getCapability(capability, facing);
            if (handle != null)
                return handle;
        }
        return null;
    }

}

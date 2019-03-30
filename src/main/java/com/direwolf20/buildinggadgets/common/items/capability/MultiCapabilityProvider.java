package com.direwolf20.buildinggadgets.common.items.capability;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Universal capability provider that links multiple capability provider to share all of their properties.
 * <p>
 * If any of the child providers returns {@code true} on {@link ICapabilityProvider#hasCapability(Capability, EnumFacing)}, it will return true.
 * If any of the child providers return a nonnull result for {@link ICapabilityProvider#getCapability(Capability, EnumFacing)}, it will return it.
 * </p>
 * <p>This means the order of providers matters, where it determines the first capability you will get if multiple child providers works some set of parameters.</p>
 */
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

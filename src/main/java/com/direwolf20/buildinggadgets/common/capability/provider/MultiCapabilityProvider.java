package com.direwolf20.buildinggadgets.common.capability.provider;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Universal capability provider that links multiple capability provider to share all of their properties.
 * <p>This means the order of providers matters, where it determines the first capability you will get if multiple child providers works some set of parameters.</p>
 */
@Tainted(reason = "Not required in any way")
public class MultiCapabilityProvider implements ICapabilityProvider {

    private final ImmutableList<ICapabilityProvider> childProviders;

    public MultiCapabilityProvider(ICapabilityProvider... childProviders) {
        this(ImmutableList.copyOf(childProviders));
    }

    public MultiCapabilityProvider(ImmutableList<ICapabilityProvider> childProviders) {
        this.childProviders = childProviders;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        for (ICapabilityProvider provider : childProviders) {
            LazyOptional<T> optional = provider.getCapability(cap, side);
            if (optional.isPresent()) return optional;
        }
        return LazyOptional.empty();
    }
}

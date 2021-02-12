package com.direwolf20.buildinggadgets.common.capability.template;

import com.direwolf20.buildinggadgets.common.capability.OurCapabilities;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TemplateProviderCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<ITemplateProvider> opt;

    public TemplateProviderCapabilityProvider(ITemplateProvider provider) {
        this.opt = LazyOptional.of(() -> provider);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == OurCapabilities.TEMPLATE_PROVIDER_CAPABILITY)
            return opt.cast();
        return LazyOptional.empty();
    }
}

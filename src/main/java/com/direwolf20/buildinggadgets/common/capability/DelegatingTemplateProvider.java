package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DelegatingTemplateProvider implements ICapabilityProvider {
    private final LazyOptional<ITemplate> lazyOpt;

    public DelegatingTemplateProvider() {
        lazyOpt = LazyOptional.of(() -> new DelegatingTemplate(ImmutableTemplate.create()));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityTemplate.TEMPLATE_CAPABILITY)
            return lazyOpt.cast();
        return LazyOptional.empty();
    }
}

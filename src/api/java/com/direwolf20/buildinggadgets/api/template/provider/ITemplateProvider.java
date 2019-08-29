package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

public interface ITemplateProvider {
    ITemplate getTemplateForKey(ITemplateKey key);

    default <T extends Throwable> ITemplate getTemplateForKey(ICapabilityProvider provider, NonNullSupplier<? extends T> exceptionSupplier) throws T {
        return getTemplateForKey(provider.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).orElseThrow(exceptionSupplier));
    }
}

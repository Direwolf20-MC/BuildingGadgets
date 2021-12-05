package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityTemplate {
    public static final Capability<ITemplateProvider> TEMPLATE_PROVIDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ITemplateKey> TEMPLATE_KEY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
}

package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import net.minecraftforge.common.capabilities.Capability;

public class CapabilityTemplate {
//    @CapabilityInject(ITemplateProvider.class)
    public static Capability<ITemplateProvider> TEMPLATE_PROVIDER_CAPABILITY = null;
//
//    @CapabilityInject(ITemplateKey.class)
    public static Capability<ITemplateKey> TEMPLATE_KEY_CAPABILITY = null;

    public static void register() {
        BuildingGadgets.LOG.debug("Registering TemplateItem Provider Capability");
//        CapabilityManager.INSTANCE.register(ITemplateProvider.class);

        BuildingGadgets.LOG.debug("Registering TemplateItem Key Capability");
//        CapabilityManager.INSTANCE.register(ITemplateKey.class);
    }
}

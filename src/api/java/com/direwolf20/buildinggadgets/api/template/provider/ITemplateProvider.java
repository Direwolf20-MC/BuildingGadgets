package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.UUID;

public interface ITemplateProvider {
    UUID getId(ITemplateKey key);

    ITemplate getTemplateForKey(ITemplateKey key);

    default <T extends Throwable> ITemplate getTemplateForKey(ICapabilityProvider provider, NonNullSupplier<? extends T> exceptionSupplier) throws T {
        return getTemplateForKey(provider.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).orElseThrow(exceptionSupplier));
    }

    /**
     * Overrides the Template for the given key.
     * <p>
     * Please prefer using TemplateTransactions and only use this if you absolutely have to (f.e. this is used for syncing).
     * This is, because if you use this Method and someone else is running a Transaction on the previous value associated with key
     * the result will silently not show up!
     *
     * @param key      The key for which the Template should be set
     * @param template The Template to set
     */
    void setTemplate(ITemplateKey key, ITemplate template);

    boolean requestUpdate(ITemplateKey key);

    boolean requestRemoteUpdate(ITemplateKey key);

    /**
     * Registers an Update Listener - it will only be weakly referenced!
     */
    void registerUpdateListener(IUpdateListener listener);

    void removeUpdateListener(IUpdateListener listener);

    interface IUpdateListener {
        default void onTemplateUpdate(ITemplateProvider provider, ITemplateKey key, ITemplate template) {

        }

        default void onTemplateUpdateSend(ITemplateProvider provider, ITemplateKey key, ITemplate template) {

        }


    }
}

package com.direwolf20.buildinggadgets.api.capability;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;
import com.direwolf20.buildinggadgets.api.template.provider.InMemoryTemplateProvider;
import com.direwolf20.buildinggadgets.api.template.provider.SimpleTemplateKey;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityTemplate {
    @CapabilityInject(ITemplateProvider.class)
    public static Capability<ITemplateProvider> TEMPLATE_PROVIDER_CAPABILITY = null;
    @CapabilityInject(ITemplateKey.class)
    public static Capability<ITemplateKey> TEMPLATE_KEY_CAPABILITY = null;

    public static void register() {
        BuildingGadgetsAPI.LOG.debug("Registering Template Provider Capability");
        CapabilityManager.INSTANCE.register(ITemplateProvider.class, new IStorage<ITemplateProvider>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<ITemplateProvider> capability, ITemplateProvider instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<ITemplateProvider> capability, ITemplateProvider instance, Direction side, INBT nbt) {

            }
        }, InMemoryTemplateProvider::new);
        BuildingGadgetsAPI.LOG.debug("Registering Template Key Capability");
        CapabilityManager.INSTANCE.register(ITemplateKey.class, new Capability.IStorage<ITemplateKey>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability capability, ITemplateKey instance, Direction side) {
                if (instance instanceof SimpleTemplateKey) {
                    SimpleTemplateKey simpleTemplateKey = (SimpleTemplateKey) instance;
                    CompoundNBT nbt = new CompoundNBT();
                    if (simpleTemplateKey.getId() != null)
                        nbt.putUniqueId(NBTKeys.KEY_ID, simpleTemplateKey.getId());
                    nbt.putBoolean("client_update", simpleTemplateKey.requireClientUpdate());
                    nbt.putBoolean("server_update", simpleTemplateKey.requireServerUpdate());
                    return nbt;
                }
                return null;
            }

            @Override
            public void readNBT(Capability capability, ITemplateKey instance, Direction side, INBT inbt) {
                if (instance instanceof SimpleTemplateKey && inbt instanceof CompoundNBT) {
                    SimpleTemplateKey simpleTemplateKey = (SimpleTemplateKey) instance;
                    CompoundNBT nbt = (CompoundNBT) inbt;
                    if (nbt.hasUniqueId(NBTKeys.KEY_ID))
                        simpleTemplateKey.setUUID(nbt.getUniqueId(NBTKeys.KEY_ID));
                    simpleTemplateKey.setRequireClientUpdate(nbt.getBoolean("client_update"));
                    simpleTemplateKey.setRequireServerUpdate(nbt.getBoolean("server_update"));
                }
            }
        }, SimpleTemplateKey::new);
    }
}

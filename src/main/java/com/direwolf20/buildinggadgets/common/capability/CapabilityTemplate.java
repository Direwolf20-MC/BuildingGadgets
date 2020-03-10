package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.template.InMemoryTemplateProvider;
import com.direwolf20.buildinggadgets.common.template.TemplateKey;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
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
        BuildingGadgets.LOG.debug("Registering TemplateItem Provider Capability");
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

        BuildingGadgets.LOG.debug("Registering TemplateItem Key Capability");
        CapabilityManager.INSTANCE.register(ITemplateKey.class, new Capability.IStorage<ITemplateKey>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability capability, ITemplateKey instance, Direction side) {
                if (instance instanceof TemplateKey) {
                    TemplateKey templateKey = (TemplateKey) instance;
                    CompoundNBT nbt = new CompoundNBT();
                    if (templateKey.getId() != null)
                        nbt.putUniqueId(com.direwolf20.buildinggadgets.common.util.ref.NBTKeys.KEY_ID, templateKey.getId());
                    return nbt;
                }
                return null;
            }

            @Override
            public void readNBT(Capability capability, ITemplateKey instance, Direction side, INBT inbt) {
                if (instance instanceof TemplateKey && inbt instanceof CompoundNBT) {
                    TemplateKey templateKey = (TemplateKey) instance;
                    CompoundNBT nbt = (CompoundNBT) inbt;
                    if (nbt.hasUniqueId(com.direwolf20.buildinggadgets.common.util.ref.NBTKeys.KEY_ID))
                        templateKey.setUUID(nbt.getUniqueId(NBTKeys.KEY_ID));
                }
            }
        }, TemplateKey::new);
    }
}

package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.InMemoryTemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateKey;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
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
            public Tag writeNBT(Capability<ITemplateProvider> capability, ITemplateProvider instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<ITemplateProvider> capability, ITemplateProvider instance, Direction side, Tag nbt) {

            }
        }, InMemoryTemplateProvider::new);

        BuildingGadgets.LOG.debug("Registering TemplateItem Key Capability");
        CapabilityManager.INSTANCE.register(ITemplateKey.class, new Capability.IStorage<ITemplateKey>() {
            @Nullable
            @Override
            public Tag writeNBT(Capability capability, ITemplateKey instance, Direction side) {
                if (instance instanceof TemplateKey) {
                    TemplateKey templateKey = (TemplateKey) instance;
                    CompoundTag nbt = new CompoundTag();
                    if (templateKey.getId() != null)
                        nbt.putUUID(com.direwolf20.buildinggadgets.common.util.ref.NBTKeys.KEY_ID, templateKey.getId());
                    return nbt;
                }
                return null;
            }

            @Override
            public void readNBT(Capability capability, ITemplateKey instance, Direction side, Tag inbt) {
                if (instance instanceof TemplateKey && inbt instanceof CompoundTag) {
                    TemplateKey templateKey = (TemplateKey) instance;
                    CompoundTag nbt = (CompoundTag) inbt;
                    if (nbt.hasUUID(com.direwolf20.buildinggadgets.common.util.ref.NBTKeys.KEY_ID))
                        templateKey.setUUID(nbt.getUUID(NBTKeys.KEY_ID));
                }
            }
        }, TemplateKey::new);
    }
}

package com.direwolf20.buildinggadgets.api.capability;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import net.minecraft.nbt.INBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityTemplate {
    @CapabilityInject(ITemplate.class)
    public static Capability<ITemplate> TEMPLATE_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ITemplate.class, new Capability.IStorage<ITemplate>() {
            @Override
            public INBTBase writeNBT(Capability<ITemplate> capability, ITemplate instance, EnumFacing side) {
                return instance.getSerializer().serialize(instance, true);
            }

            @Override
            public void readNBT(Capability<ITemplate> capability, ITemplate instance, EnumFacing side, INBTBase base) {
                //TODO perform copy Transaction on the original instance
            }
        }, ImmutableTemplate::create);
    }
}

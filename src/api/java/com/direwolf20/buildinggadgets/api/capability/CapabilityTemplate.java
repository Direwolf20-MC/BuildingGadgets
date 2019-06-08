package com.direwolf20.buildinggadgets.api.capability;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityTemplate {
    @CapabilityInject(ITemplate.class)
    public static Capability<ITemplate> TEMPLATE_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ITemplate.class, new Capability.IStorage<ITemplate>() {
            @Override
            public INBT writeNBT(Capability<ITemplate> capability, ITemplate instance, Direction side) {
                return instance.getSerializer().serialize(instance, true);
            }

            @Override
            public void readNBT(Capability<ITemplate> capability, ITemplate instance, Direction side, INBT base) {
                //TODO perform copy Transaction on the original instance
            }
        }, ImmutableTemplate::create);
    }
}

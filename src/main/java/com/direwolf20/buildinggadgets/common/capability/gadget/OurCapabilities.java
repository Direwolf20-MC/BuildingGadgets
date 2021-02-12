package com.direwolf20.buildinggadgets.common.capability.gadget;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class OurCapabilities {
    @CapabilityInject(GadgetMeta.class)
    public static Capability<GadgetMeta> GADGET_META = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(GadgetMeta.class, new Capability.IStorage<GadgetMeta>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability capability, GadgetMeta instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability capability, GadgetMeta instance, Direction side, INBT inbt) { }
        }, () -> new GadgetMeta(null));
    }
}

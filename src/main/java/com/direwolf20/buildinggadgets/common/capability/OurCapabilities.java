package com.direwolf20.buildinggadgets.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class OurCapabilities {
    @CapabilityInject(GadgetMetaCapability.class)
    public static Capability<GadgetMetaCapability> GADGET_META = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(GadgetMetaCapability.class, new Capability.IStorage<GadgetMetaCapability>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability capability, GadgetMetaCapability instance, Direction side) {
                return instance.serialize();
            }

            @Override
            public void readNBT(Capability capability, GadgetMetaCapability instance, Direction side, INBT inbt) {
                instance.deserialize((CompoundNBT) inbt);
            }
        }, GadgetMetaCapability::new);
    }
}

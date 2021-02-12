package com.direwolf20.buildinggadgets.common.capability.gadget;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class OurCapabilities {
    @CapabilityInject(IGadgetMeta.class)
    public static Capability<IGadgetMeta> GADGET_META = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IGadgetMeta.class, new Capability.IStorage<IGadgetMeta>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability capability, IGadgetMeta instance, Direction side) {
                return instance.serialize();
            }

            @Override
            public void readNBT(Capability capability, IGadgetMeta instance, Direction side, INBT inbt) {
                instance.deserialize((CompoundNBT) inbt);
            }
        }, () -> new GadgetMeta(null));
    }
}

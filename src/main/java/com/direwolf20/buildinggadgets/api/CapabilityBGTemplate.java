package com.direwolf20.buildinggadgets.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityBGTemplate {
    @CapabilityInject(IMutableTemplate.class)
    public static Capability<IMutableTemplate> CAPABILITY_MUTABLE_TEMPLATE = null;
    @CapabilityInject(ITemplate.class)
    public static Capability<ITemplate> CAPABILITY_TEMPLATE = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ITemplate.class, new IStorage<ITemplate>() {
            @Override
            public NBTBase writeNBT(Capability<ITemplate> capability, ITemplate instance, EnumFacing side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<ITemplate> capability, ITemplate instance, EnumFacing side, NBTBase base) {
                throw new UnsupportedOperationException();
            }
        }, Template::new);
        CapabilityManager.INSTANCE.register(IMutableTemplate.class, new IStorage<IMutableTemplate>() {
            @Override
            public NBTBase writeNBT(Capability<IMutableTemplate> capability, IMutableTemplate instance, EnumFacing side) {
                if (instance instanceof INBTSerializable) {
                    return ((INBTSerializable) instance).serializeNBT();
                }
                throw new UnsupportedOperationException();
            }

            @Override
            @SuppressWarnings("unchecked") //We can't do anything if someone passes in the wrong type...
            public void readNBT(Capability<IMutableTemplate> capability, IMutableTemplate instance, EnumFacing side, NBTBase base) {
                if (instance instanceof INBTSerializable) {
                    ((INBTSerializable) instance).deserializeNBT(base);
                }
                throw new UnsupportedOperationException();
            }
        }, () -> new MutableTemplate(WorldSave.CHANGING_STORAGE));
    }
}

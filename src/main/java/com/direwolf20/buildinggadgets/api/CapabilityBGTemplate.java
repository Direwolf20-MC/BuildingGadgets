package com.direwolf20.buildinggadgets.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.UUID;

public class CapabilityBGTemplate {
    private static final String KEY_ID = "save_id";
    @CapabilityInject(ITemplate.class)
    public static Capability<ITemplate> CAPABILITY_TEMPLATE;

    public static void register() {
        CapabilityManager.INSTANCE.register(ITemplate.class, new IStorage<ITemplate>() {
            @Override
            public NBTBase writeNBT(Capability<ITemplate> capability, ITemplate instance, EnumFacing side) {

                return null;
            }

            @Override
            public void readNBT(Capability<ITemplate> capability, ITemplate instance, EnumFacing side, NBTBase base) {
                if (!(base instanceof NBTTagCompound)) {
                    throw new IllegalArgumentException("Expected NBTTagCompound!");
                }
                UUID id = instance.getID();
                if (id != null) {

                }
            }
        }, Template::new);
    }
}

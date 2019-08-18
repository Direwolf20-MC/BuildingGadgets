package com.direwolf20.buildinggadgets.api.capability;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityTemplate {
    @CapabilityInject(ITemplate.class)
    public static Capability<ITemplate> TEMPLATE_CAPABILITY = null;

    public static void register() {
        BuildingGadgetsAPI.LOG.debug("Registering Template Capability");
        CapabilityManager.INSTANCE.register(ITemplate.class, new Capability.IStorage<ITemplate>() {
            @Override
            public INBT writeNBT(Capability<ITemplate> capability, ITemplate instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString(NBTKeys.KEY_SERIALIZER, instance.getSerializer().getRegistryName().toString());
                nbt.put(NBTKeys.KEY_DATA, instance.getSerializer().serialize(instance, true));
                return nbt;
            }

            @Override
            public void readNBT(Capability<ITemplate> capability, ITemplate instance, Direction side, INBT base) {

            }
        }, ImmutableTemplate::create);
    }
}

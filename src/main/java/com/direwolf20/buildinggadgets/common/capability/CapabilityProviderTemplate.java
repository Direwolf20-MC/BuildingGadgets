package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.api.CapabilityBGTemplate;
import com.direwolf20.buildinggadgets.api.ItemTemplate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderTemplate implements ICapabilityProvider {
    private ItemTemplate template;

    public CapabilityProviderTemplate(ItemStack stack) {
        this.template = new ItemTemplate();
        NBTTagCompound compound = stack.getTagCompound();
        template.readItemShareNBT(compound != null ? stack.getTagCompound() : new NBTTagCompound());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityBGTemplate.CAPABILITY_TEMPLATE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return hasCapability(capability, facing) ? CapabilityBGTemplate.CAPABILITY_TEMPLATE.cast(template) : null;
    }
}

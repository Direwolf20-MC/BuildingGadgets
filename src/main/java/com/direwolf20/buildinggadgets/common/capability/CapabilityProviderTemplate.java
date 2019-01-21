package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.api.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderTemplate implements ICapabilityProvider {
    private MutableTemplate template;

    public CapabilityProviderTemplate(ITemplateDataStorage storage) {
        this.template = new MutableTemplate(storage);
    }

    @Nonnull
    public static ITemplate tryLoadTemplate(ItemStack stack, World world) {
        ITemplate template = tryLoadPossibleTemplate(stack, world);
        if (template == null) {
            throw new RuntimeException("Expected ItemStack to expose Template Capability, but no Template could be retrieved!");
        }
        return template;
    }

    @Nullable
    public static ITemplate tryLoadPossibleTemplate(ItemStack stack, World world) {
        ITemplate template = getPossibleTemplate(stack, world);
        if (template == null) template = stack.getCapability(CapabilityBGTemplate.CAPABILITY_TEMPLATE, null);
        return template;
    }

    @Nullable
    public static IMutableTemplate getPossibleTemplate(ItemStack stack, World world) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot retrieve the Template for a Empty Stack!");
        }
        IMutableTemplate template = stack.getCapability(CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE, null);
        if (template == null) return null;
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) tagCompound = new NBTTagCompound();
        template.readNBT(tagCompound, world);
        return template;
    }

    @Nonnull
    public static IMutableTemplate getTemplate(ItemStack stack, World world) {
        IMutableTemplate template = getPossibleTemplate(stack, world);
        if (template == null) {
            throw new RuntimeException("Expected ItemStack to expose Template Capability, but no Template could be retrieved!");
        }
        return template;
    }

    public static void writeTemplate(IMutableTemplate template, ItemStack stack, World world) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) tagCompound = new NBTTagCompound();
        template.writeNBT(tagCompound, world);
        stack.setTagCompound(tagCompound);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityBGTemplate.CAPABILITY_TEMPLATE || capability == CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityBGTemplate.CAPABILITY_TEMPLATE) {
            return CapabilityBGTemplate.CAPABILITY_TEMPLATE.cast(template);
        } else if (capability == CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE) {
            return CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE.cast(template);
        }
        return null;
    }
}

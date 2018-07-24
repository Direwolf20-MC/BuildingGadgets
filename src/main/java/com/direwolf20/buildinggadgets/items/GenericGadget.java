package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.items.ItemCaps.CapabilityProviderEnergy;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GenericGadget extends Item {


    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound tag) {
        return new CapabilityProviderEnergy(stack, Config.energyMax);
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);

        return 1D - ((double) energy.getEnergyStored() / (double) energy.getMaxEnergyStored());
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);

        return MathHelper.hsvToRGB(Math.max(0.0F, (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored()) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
        return energy.getEnergyStored() != energy.getMaxEnergyStored();
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
        return energy.getEnergyStored() != energy.getMaxEnergyStored();
    }
}

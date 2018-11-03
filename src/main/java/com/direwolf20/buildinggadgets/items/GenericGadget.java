package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.items.ItemCaps.CapabilityProviderEnergy;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class GenericGadget extends Item {

    public int getEnergyMax() {
        return Config.energyMax;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound tag) {
        if (Config.poweredByFE) {
            return new CapabilityProviderEnergy(stack, this.getEnergyMax());
        }
        return null;
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
        if (Config.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return 1D - ((double) energy.getEnergyStored() / (double) energy.getMaxEnergyStored());
        }
        //return (double)stack.getItemDamage() / (double)stack.getMaxDamage();
        return super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        if (Config.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return MathHelper.hsvToRGB(Math.max(0.0F, (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored()) / 3.0F, 1.0F, 1.0F);
        }
        //return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        if (Config.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return energy.getEnergyStored() != energy.getMaxEnergyStored();
        }
        //return (stack.getItemDamage() > 0);
        return super.isDamaged(stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (Config.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return energy.getEnergyStored() != energy.getMaxEnergyStored();
        }
        //return stack.isItemDamaged();
        return super.showDurabilityBar(stack);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (Config.poweredByFE) {
            return false;
        }
        if (repair.getItem() == Items.DIAMOND) {
            return true;
        }
        return false;
    }
}

package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class GadgetGeneric extends Item {

    public GadgetGeneric(Builder properties) {
        super(properties);

//        this.getCreativeTabs().add(this.getGroup());
    }

    public int getEnergyMax() {
        return SyncedConfig.energyMax;
    }
//
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound tag) {
        if (SyncedConfig.poweredByFE) {
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
        if (SyncedConfig.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return 1D - ((double) energy.getEnergyStored() / (double) energy.getMaxEnergyStored());
        }


        return super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        if (SyncedConfig.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return MathHelper.hsvToRGB(Math.max(0.0F, (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored()) / 3.0F, 1.0F, 1.0F);
        }
        //return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        if (SyncedConfig.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return energy.getEnergyStored() != energy.getMaxEnergyStored();
        }
        //return (stack.getItemDamage() > 0);
        return super.isDamaged(stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (SyncedConfig.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return energy.getEnergyStored() != energy.getMaxEnergyStored();
        }
        //return stack.isItemDamaged();
        return super.showDurabilityBar(stack);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (SyncedConfig.poweredByFE) {
            return false;
        }
        if (repair.getItem() == Items.DIAMOND) {
            return true;
        }
        return false;
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof GadgetGeneric)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof GadgetGeneric)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public int getEnergyCost() {
        return 200;
    }

    public int getDamagePerUse() {
        return 1;
    }

    public boolean canUse(ItemStack tool, EntityPlayer player) {
        if (player.isCreative())
            return true;

        if (SyncedConfig.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(tool);
            return this.getEnergyCost() <= energy.getEnergyStored();
        }
        return tool.getDamage() < tool.getMaxDamage() || tool.getStack().isDamageable();
    }

    public void applyDamage(ItemStack tool, EntityPlayer player) {
        if( SyncedConfig.poweredByFE ) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(tool);
            energy.extractEnergy(this.getEnergyCost(), false);
        }
        else
            tool.damageItem(this.getDamagePerUse(), player);
    }
}

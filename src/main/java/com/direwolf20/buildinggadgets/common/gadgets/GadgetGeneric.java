package com.direwolf20.buildinggadgets.common.gadgets;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.ItemModBase;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.config.utils.NBTTool;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.withSuffix;

public abstract class GadgetGeneric extends ItemModBase {

    public GadgetGeneric(String name) {
        super(name);
        setMaxStackSize(1);
    }

    public int getEnergyMax() {
        return SyncedConfig.energyMax;
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound tag) {
        return new CapabilityProviderEnergy(stack, this::getEnergyMax);
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
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return 1D - ((double) energy.getEnergyStored() / (double) energy.getMaxEnergyStored());
        }
        //return (double)stack.getItemDamage() / (double)stack.getMaxDamage();
        return super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return MathHelper.hsvToRGB(Math.max(0.0F, (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored()) / 3.0F, 1.0F, 1.0F);
        }
        //return MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return energy.getEnergyStored() != energy.getMaxEnergyStored();
        }
        //return (stack.getItemDamage() > 0);
        return super.isDamaged(stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("creative", Constants.NBT.TAG_BYTE))
            return false;

        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            return energy.getEnergyStored() != energy.getMaxEnergyStored();
        }
        //return stack.isItemDamaged();
        return super.showDurabilityBar(stack);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (toRepair.hasCapability(CapabilityEnergy.ENERGY, null))
            return false;
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

    public abstract int getEnergyCost(ItemStack tool);

    public abstract int getDamageCost(ItemStack tool);

    public boolean canUse(ItemStack tool, EntityPlayer player) {
        if (player.capabilities.isCreativeMode)
            return true;

        if (tool.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(tool);
            return getEnergyCost(tool) <= energy.getEnergyStored();
        }
        return tool.getMaxDamage() <= 0 || tool.getItemDamage() < tool.getMaxDamage() || tool.isItemStackDamageable();
    }

    public void applyDamage(ItemStack tool, EntityPlayer player) {
        if(tool.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(tool);
            energy.extractEnergy(getEnergyCost(tool), false);
        }
        else
            tool.damageItem(getDamageCost(tool), player);
    }

    protected void addEnergyInformation(List<String> list, ItemStack stack) {
        if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
        }
    }

    public static boolean getFuzzy(ItemStack stack) {
        return getOrNewTag(stack).getBoolean("fuzzy");
    }

    public static void toggleFuzzy(EntityPlayer player, ItemStack stack) {
        getOrNewTag(stack).setBoolean("fuzzy", !getFuzzy(stack));
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.fuzzymode").getUnformattedComponentText() + ": " + getFuzzy(stack)), true);
    }

    public static boolean getConnectedArea(ItemStack stack) {
        return !getOrNewTag(stack).getBoolean("unconnectedarea");
    }

    public static void toggleConnectedArea(EntityPlayer player, ItemStack stack) {
        getOrNewTag(stack).setBoolean("unconnectedarea", getConnectedArea(stack));
        String suffix = stack.getItem() instanceof GadgetDestruction ? "area" : "surface";
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.connected" + suffix).getUnformattedComponentText() + ": " + getConnectedArea(stack)), true);
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return getOrNewTag(stack).getBoolean("raytrace_fluid");
    }

    public static void toggleRayTraceFluid(EntityPlayer player, ItemStack stack) {
        getOrNewTag(stack).setBoolean("raytrace_fluid", !shouldRayTraceFluid(stack));
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.raytrace_fluid").getUnformattedComponentText() + ": " + shouldRayTraceFluid(stack)), true);
    }

    public static void addInformationRayTraceFluid(List<String> tooltip, ItemStack stack) {
        tooltip.add(TextFormatting.BLUE + I18n.format("tooltip.gadget.raytrace_fluid") + ": " + shouldRayTraceFluid(stack));
    }

    /**
     * If the given stack has a tag, returns it. If the given stack does not have a tag, it will set a reference and return the new tag
     * compound.
     */
    public static NBTTagCompound getOrNewTag(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound();
        }
        NBTTagCompound tag = new NBTTagCompound();
        stack.setTagCompound(tag);
        return tag;
    }
}

package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.utils.CapabilityUtil.EnergyUtil;
import com.direwolf20.buildinggadgets.common.utils.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.utils.helpers.NBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

import static com.direwolf20.buildinggadgets.common.utils.GadgetUtils.withSuffix;

public abstract class GadgetGeneric extends Item {

    public GadgetGeneric(Properties builder) {
        super(builder);
    }

    public abstract int getEnergyMax();

    public abstract int getEnergyCost(ItemStack tool);

    public abstract int getDamageCost(ItemStack tool);

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

    public static boolean poweredByFE() {
        return Config.GADGETS.poweredByFE.get();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return EnergyUtil.returnDoubleIfPresent(stack,
                (energy -> 1D - (energy.getEnergyStored() / (double) energy.getMaxEnergyStored())),
                () -> super.getDurabilityForDisplay(stack));
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return EnergyUtil.returnIntIfPresent(stack,
                (energy -> MathHelper.hsvToRGB(Math.max(0.0F, energy.getEnergyStored() / (float) energy.getMaxEnergyStored()) / 3.0F, 1.0F, 1.0F)),
                () -> super.getRGBDurabilityForDisplay(stack));
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return EnergyUtil.returnBooleanIfPresent(stack,
                energy -> energy.getEnergyStored() != energy.getMaxEnergyStored(),
                () -> super.isDamaged(stack));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("creative", Constants.NBT.TAG_BYTE))
            return false;

        return EnergyUtil.returnBooleanIfPresent(stack,
                energy -> energy.getEnergyStored() != energy.getMaxEnergyStored(),
                () -> super.showDurabilityBar(stack));
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return ! EnergyUtil.hasCap(toRepair) && repair.getItem() == Items.DIAMOND;
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

    public boolean canUse(ItemStack tool, EntityPlayer player) {
        if (player.isCreative())
            return true;

        if (poweredByFE()) {
            IEnergyStorage energy = EnergyUtil.getCap(tool).orElseThrow(CapabilityNotPresentException::new);
            return getEnergyCost(tool) <= energy.getEnergyStored();
        }
        return tool.getDamage() < tool.getMaxDamage() || tool.getStack().isDamageable();
    }

    public void applyDamage(ItemStack tool, EntityPlayer player) {
        if (poweredByFE()) {
            IEnergyStorage energy = EnergyUtil.getCap(tool).orElseThrow(CapabilityNotPresentException::new);
            energy.extractEnergy(getEnergyCost(tool), false);
        }
        else
            tool.damageItem(getDamageCost(tool), player);
    }

    protected void addEnergyInformation(List<ITextComponent> tooltip, ItemStack stack) {
        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
            tooltip.add(new TextComponentString(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored())));
        });
    }

    public static boolean getFuzzy(ItemStack stack) {
        return NBTHelper.getOrNewTag(stack).getBoolean("fuzzy");
    }

    public static void toggleFuzzy(EntityPlayer player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).setBoolean("fuzzy", !getFuzzy(stack));
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.fuzzymode").getUnformattedComponentText() + ": " + getFuzzy(stack)), true);
    }

    public static boolean getConnectedArea(ItemStack stack) {
        return !NBTHelper.getOrNewTag(stack).getBoolean("unconnectedarea");
    }

    public static void toggleConnectedArea(EntityPlayer player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).setBoolean("unconnectedarea", getConnectedArea(stack));
        String suffix = stack.getItem() instanceof GadgetDestruction ? "area" : "surface";
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.connected" + suffix).getUnformattedComponentText() + ": " + getConnectedArea(stack)), true);
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return NBTHelper.getOrNewTag(stack).getBoolean("raytrace_fluid");
    }

    public static void toggleRayTraceFluid(EntityPlayer player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).setBoolean("raytrace_fluid", !shouldRayTraceFluid(stack));
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.raytrace_fluid").getUnformattedComponentText() + ": " + shouldRayTraceFluid(stack)), true);
    }

    public static void addInformationRayTraceFluid(List<ITextComponent> tooltip, ItemStack stack) {
        tooltip.add(new TextComponentString(TextFormatting.BLUE + I18n.format("tooltip.gadget.raytrace_fluid") + ": " + shouldRayTraceFluid(stack)));
    }
    
    protected static String formatName(String name) {
        return name.replaceAll("(?=[A-Z])", " ").trim();
    }
}

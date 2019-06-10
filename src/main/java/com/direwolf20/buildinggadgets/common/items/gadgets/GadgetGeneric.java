package com.direwolf20.buildinggadgets.common.items.gadgets;


import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderBlockProvider;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.items.capability.MultiCapabilityProvider;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil.EnergyUtil;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

public abstract class GadgetGeneric extends Item {
    private static final IItemPropertyGetter DAMAGED_GETTER =
            (p_210306_0_, p_210306_1_, p_210306_2_) -> p_210306_0_.isDamaged() ? 1.0F : 0.0F;
    private static final IItemPropertyGetter DAMAGE_GETTER =
            (p_210307_0_, p_210307_1_, p_210307_2_) -> MathHelper
                    .clamp((float) p_210307_0_.getDamage() / (float) p_210307_0_.getMaxDamage(), 0.0F, 1.0F);

    public GadgetGeneric(Properties builder) {
        super(builder);
        this.addPropertyOverride(new ResourceLocation("damaged"), DAMAGED_GETTER);
        this.addPropertyOverride(new ResourceLocation("damage"), DAMAGE_GETTER);
    }

    public abstract int getEnergyMax();

    public abstract int getEnergyCost(ItemStack tool);

    public abstract int getDamageCost(ItemStack tool);

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT tag) {
        return new MultiCapabilityProvider(new CapabilityProviderEnergy(stack, this::getEnergyMax), new CapabilityProviderBlockProvider(stack));
    }

    @Override
    public boolean isDamageable() {
        return getMaxDamage() > 0;
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
        if (stack.hasTag() && stack.getTag().contains(NBTKeys.CREATIVE_MARKER))
            return false;

        return EnergyUtil.returnBooleanIfPresent(stack,
                energy -> energy.getEnergyStored() != energy.getMaxEnergyStored(),
                () -> super.showDurabilityBar(stack));
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return !EnergyUtil.hasCap(toRepair) && repair.getItem() == Items.DIAMOND;
    }

    public static ItemStack getGadget(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof GadgetGeneric)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof GadgetGeneric)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public boolean canUse(ItemStack tool, PlayerEntity player) {
        if (player.isCreative())
            return true;

        if (poweredByFE()) {
            IEnergyStorage energy = EnergyUtil.getCap(tool).orElseThrow(CapabilityNotPresentException::new);
            return getEnergyCost(tool) <= energy.getEnergyStored();
        }
        return tool.getDamage() < tool.getMaxDamage() || tool.getStack().isDamageable();
    }

    public void applyDamage(ItemStack tool, ServerPlayerEntity player) {
        if (poweredByFE()) {
            IEnergyStorage energy = EnergyUtil.getCap(tool).orElseThrow(CapabilityNotPresentException::new);
            energy.extractEnergy(getEnergyCost(tool), false);
        } else
            tool.attemptDamageItem(getDamageCost(tool), new Random(), player);
    }

    protected void addEnergyInformation(List<ITextComponent> tooltip, ItemStack stack) {
        if (Config.isServerConfigLoaded())
            stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
                tooltip.add(TooltipTranslation.GADGET_ENERGY
                                    .componentTranslation(withSuffix(energy.getEnergyStored()), withSuffix(energy.getMaxEnergyStored()))
                                    .setStyle(Styles.WHITE));
            });
    }

    public static boolean getFuzzy(ItemStack stack) {
        return NBTHelper.getOrNewTag(stack).getBoolean(NBTKeys.GADGET_FUZZY);
    }

    public static void toggleFuzzy(PlayerEntity player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).putBoolean(NBTKeys.GADGET_FUZZY, !getFuzzy(stack));
        player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.fuzzymode").getUnformattedComponentText() + ": " + getFuzzy(stack)), true);
    }

    public static boolean getConnectedArea(ItemStack stack) {
        return !NBTHelper.getOrNewTag(stack).getBoolean(NBTKeys.GADGET_UNCONNECTED_AREA);
    }

    public static void toggleConnectedArea(PlayerEntity player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).putBoolean(NBTKeys.GADGET_UNCONNECTED_AREA, getConnectedArea(stack));
        String suffix = stack.getItem() instanceof GadgetDestruction ? "area" : "surface";
        player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.connected" + suffix).getUnformattedComponentText() + ": " + getConnectedArea(stack)), true);
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return NBTHelper.getOrNewTag(stack).getBoolean(NBTKeys.GADGET_RAYTRACE_FLUID);
    }

    public static void toggleRayTraceFluid(ServerPlayerEntity player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).putBoolean(NBTKeys.GADGET_RAYTRACE_FLUID, !shouldRayTraceFluid(stack));
        player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.raytrace_fluid").getUnformattedComponentText() + ": " + shouldRayTraceFluid(stack)), true);
    }

    public static void addInformationRayTraceFluid(List<ITextComponent> tooltip, ItemStack stack) {
        tooltip.add(TooltipTranslation.GADGET_RAYTRACE_FLUID
                            .componentTranslation(String.valueOf(shouldRayTraceFluid(stack)))
                            .setStyle(Styles.BLUE));
    }

    protected static String formatName(String name) {
        return name.replaceAll("(?=[A-Z])", " ").trim();
    }
}

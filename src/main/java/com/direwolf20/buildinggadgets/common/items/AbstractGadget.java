package com.direwolf20.buildinggadgets.common.items;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractGadget extends Item {
    protected final Config.CategoryGadgets.GadgetConfig config;

    public AbstractGadget(Config.CategoryGadgets.GadgetConfig config) {
        super(OurItems.itemProperties().maxStackSize(1));
        this.config = config;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (this.usesPower()) {
            // Show tooltip for Forge Energy
            if (!this.usesDurability()) {
                stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> tooltip.add(TooltipTranslation.GADGET_ENERGY
                    .componentTranslation(withSuffix(energy.getEnergyStored()), withSuffix(energy.getMaxEnergyStored()))
                    .setStyle(Styles.GRAY)));
            } else {
                // Show durability remaining for durability
                tooltip.add(TooltipTranslation.GADGET_DURABILITY.componentTranslation(stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()).setStyle(Styles.GRAY));
            }
        } else {
            tooltip.add(TooltipTranslation.GADGET_CREATIVE.componentTranslation().setStyle(Styles.LT_PURPLE));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if( !isInGroup(group) || !this.usesPower() || (this.usesPower() && this.usesDurability()) )
            return;

        ItemStack charged = new ItemStack(this);
        charged.getOrCreateTag().putDouble(NBTKeys.ENERGY, this.config.maxEnergy.get());
        items.add(charged);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new CapabilityProviderEnergy(stack, this.config.maxEnergy::get);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return this.usesPower();
    }

    /**
     * Displays a durability bar if {@link #showDurabilityBar(ItemStack)} is true
     * Switches dynamically between energy and durability.
     */
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (!this.usesPower()) {
            return 0;
        }

        if (this.usesDurability()) {
            return super.getDurabilityForDisplay(stack);
        }

        return stack.getCapability(CapabilityEnergy.ENERGY)
            .map(e -> 1D - (e.getEnergyStored() / (double) e.getMaxEnergyStored()))
            .orElse(super.getDurabilityForDisplay(stack));
    }

    /**
     * Shows the durability bar as a specific color.
     */
    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        if (!this.usesPower()) {
            return 0;
        }

        if (this.usesDurability()) {
            return super.getRGBDurabilityForDisplay(stack);
        }

        int power = stack.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
        int maxPower = stack.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(0);

        return MathHelper.hsvToRGB(Math.max(0.0F, power / (float) maxPower) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamageable() {
        return this.usesDurability();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (!this.usesPower() || !this.usesDurability()) {
            return 0;
        }

        return this.config.maxDurability.get();
    }

    /**
     * Only repairable if we're using Durability. Otherwise no.
     */
    @Override
    public boolean isRepairable(ItemStack stack) {
        return this.usesDurability();
    }

    /**
     * Dictates if we can repair the item with a given item.
     */
    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.usesDurability() && repair.getItem() == Items.DIAMOND;
    }

    /**
     * @return if we're using Energy or Durability.
     */
    private boolean usesPower() {
        return this.config.useDurability.get() || this.config.useEnergy.get();
    }

    /**
     * @return specifically if we're using durability.
     */
    private boolean usesDurability() {
        return this.config.useDurability.get();
    }
}

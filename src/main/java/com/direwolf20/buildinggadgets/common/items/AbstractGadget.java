package com.direwolf20.buildinggadgets.common.items;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

import com.direwolf20.buildinggadgets.common.capability.GadgetCapabilityProvider;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraftforge.common.util.LazyOptional;
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
                this.getEnergy(stack).ifPresent(energy -> tooltip.add(TooltipTranslation.GADGET_ENERGY
                    .componentTranslation(withSuffix(energy.getEnergyStored()), withSuffix(energy.getMaxEnergyStored()))
                    .setStyle(Styles.GRAY)));
            } else {
                // Show durability remaining for durability
                tooltip.add(TooltipTranslation.GADGET_DURABILITY.componentTranslation(stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()).setStyle(Styles.GRAY));
            }
        } else {
            // Show a creative text for anything not using energy
            tooltip.add(TooltipTranslation.GADGET_CREATIVE.componentTranslation().setStyle(Styles.LT_PURPLE));
        }
    }

    /**
     * Fill the creative tab with charged gadgets
     */
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
        return new GadgetCapabilityProvider(stack, this.config.maxEnergy::get, this instanceof CopyGadgetItem);
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

        return this.getEnergy(stack)
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

        int power = this.getEnergy(stack).map(IEnergyStorage::getEnergyStored).orElse(0);
        int maxPower = this.getEnergy(stack).map(IEnergyStorage::getMaxEnergyStored).orElse(0);

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

    /**
     * Determines if the current gadget has enough power (durability or energy)
     * to perform a single action based on it's `cost per operation`
     */
    private boolean hasRequiredPower(ItemStack stack, PlayerEntity player) {
        return hasRequiredPowerAtCost(stack, player, getCostPerOperation(player));
    }

    /**
     * Determines if the current gadget has enough power (durability or energy)
     * to perform a mass action... based on a custom cost param
     */
    private boolean hasRequiredPowerAtCost(ItemStack stack, PlayerEntity player, int costOfOperation) {
        // If we're in creative or the gadget is set to be creative then pass
        if (this.avoidsPower(player)) {
            return true;
        }

        int remainingPower = this.usesDurability()
            ? (stack.getMaxDamage() - stack.getDamage()) - costOfOperation
            : this.getEnergy(stack).map(IEnergyStorage::getEnergyStored).orElse(0) - costOfOperation;

        return remainingPower > 0;
    }

    /**
     * Gets the specific cost per operation from the config, ignored if creative or no power requirement.
     */
    private int getCostPerOperation(PlayerEntity player) {
        if (this.avoidsPower(player)) {
            return 0;
        }

        return this.usesDurability()
            ? this.config.durabilityCost.get()
            : this.config.energyCost.get();
    }

    /**
     * Performs the correct power reduction method.
     */
    private void removePower(ItemStack stack, PlayerEntity player, int amount) {
        if (this.avoidsPower(player)) {
            return;
        }

        if (this.usesDurability()) {
            this.setDamage(stack, stack.getDamage() - amount);
            return;
        }

        this.getEnergy(stack).ifPresent(energy -> energy.extractEnergy(amount, false));
    }

    private LazyOptional<IEnergyStorage> getEnergy(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY);
    }

    private boolean avoidsPower(PlayerEntity player) {
        return player.isCreative() || !this.usesPower();
    }
}

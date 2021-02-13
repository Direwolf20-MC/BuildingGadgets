package com.direwolf20.buildinggadgets.common.items;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.building.modes.EmptyMode;
import com.direwolf20.buildinggadgets.common.building.modes.SurfaceMode;
import com.direwolf20.buildinggadgets.common.capability.OurCapabilities;
import com.direwolf20.buildinggadgets.common.capability.gadget.GadgetMeta;
import com.direwolf20.buildinggadgets.common.capability.gadget.GadgetMetaProvider;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetAbilities;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetHelper;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractGadget extends Item {
    protected final Config.CategoryGadgets.GadgetConfig config;

    public AbstractGadget(Config.CategoryGadgets.GadgetConfig config) {
        super(OurItems.itemProperties().maxStackSize(1));
        this.config = config;
    }

    /**
     * Gathers together Meta information and power
     */
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (world == null) {
            return;
        }

        // Power information
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

        this.getMeta(stack).ifPresent(meta -> tooltip.addAll(this.addMetaInformation(meta, stack, world)));
    }

    /**
     * Generates tooltip information based on the Gadgets Abilities and it's Meta
     */
    List<ITextComponent> addMetaInformation(GadgetMeta meta, ItemStack stack, @Nullable World world) {
        List<ITextComponent> tips = new ArrayList<>();

        // Current selected block
        if (meta.getBlockState() != null && this.getAbilities().isSelectBlocks()) {
            tips.add(TooltipTranslation.GADGET_BLOCK.componentTranslation(meta.getBlockState().getBlock().getTranslatedName().getString()).mergeStyle(TextFormatting.GREEN));
        }

        // Mode
        if (this.getAbilities().canChangeModes() && !(meta.getMode() instanceof EmptyMode)) {
            tips.add(TooltipTranslation.GADGET_MODE
                .componentTranslation((meta.getMode().identifier() == SurfaceMode.name && meta.isConnectedArea()
                    ? TooltipTranslation.GADGET_CONNECTED.format(meta.getMode().entry().translatedName())
                    : meta.getMode().entry().translatedName()))
                .setStyle(Styles.AQUA));
        }

        // Range
        if (this.getAbilities().canChangeRange()) {
            tips.add(TooltipTranslation.GADGET_RANGE.componentTranslation(meta.getRange(), GadgetHelper.getRangeInBlocks(meta.getRange(), meta.getMode())).mergeStyle(TextFormatting.LIGHT_PURPLE));
        }

        // Fuzzy
        if (this.getAbilities().canFuzzy() && meta.getMode() instanceof SurfaceMode) {
            tips.add(TooltipTranslation.GADGET_FUZZY.componentTranslation(String.valueOf(meta.isFuzzy())).mergeStyle(TextFormatting.GOLD));
        }

        // Place on top of blocks
        if (this.getAbilities().isPlaceOnTop()) {
            tips.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP.componentTranslation(String.valueOf(meta.isPlaceOnTop())).mergeStyle(TextFormatting.YELLOW));
        }

        // Should trace water
        if (this.getAbilities().isCanTraceWater()) {
            tips.add(TooltipTranslation.GADGET_RAYTRACE_FLUID.componentTranslation(String.valueOf(meta.canFluidTrace())).mergeStyle(TextFormatting.BLUE));
        }

        return tips;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
        ActionResult<ItemStack> result;

        ItemStack gadget = player.getHeldItem(handIn);
        RayTraceResult cast = player.pick(Config.GENERAL.rayTraceRange.get(), 1.0f, this.getMeta(gadget).map(GadgetMeta::canFluidTrace).orElse(false));
        if (world.isRemote) {
            result = player.isSneaking()
                ? this.onShiftClientAction(world, player, gadget, cast)
                : this.onClientAction(world, player, gadget, cast);
        } else {
            result = player.isSneaking()
                ? this.onShiftAction(world, player, gadget, cast)
                : this.onAction(world, player, gadget, cast);
        }

        return result.getType() == ActionResultType.FAIL
            ? super.onItemRightClick(world, player, handIn)
            : result;
    }

    /**
     * Not all gadgets use all of these, so this is the best way to handle that.
     */
    ActionResult<ItemStack> onShiftClientAction(World world, PlayerEntity player, ItemStack gadget, RayTraceResult cast) {
        return ActionResult.resultPass(gadget);
    }

    ActionResult<ItemStack> onClientAction(World world, PlayerEntity player, ItemStack gadget, RayTraceResult cast) {
        return ActionResult.resultPass(gadget);
    }

    ActionResult<ItemStack> onShiftAction(World world, PlayerEntity player, ItemStack gadget, RayTraceResult cast) {
        return ActionResult.resultPass(gadget);
    }

    ActionResult<ItemStack> onAction(World world, PlayerEntity player, ItemStack gadget, RayTraceResult cast) {
        return ActionResult.resultPass(gadget);
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
        return new GadgetMetaProvider(stack, this.config.maxEnergy::get, this instanceof CopyGadgetItem);
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

    public abstract Set<IMode> getModes();

    public abstract GadgetAbilities getAbilities();

    private LazyOptional<IEnergyStorage> getEnergy(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY);
    }

    private LazyOptional<GadgetMeta> getMeta(ItemStack stack) {
        return stack.getCapability(OurCapabilities.GADGET_META);
    }

    private boolean avoidsPower(PlayerEntity player) {
        return player.isCreative() || !this.usesPower();
    }
}

package com.direwolf20.buildinggadgets.common.items.gadgets;


import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.capability.provider.CapabilityProviderBlockProvider;
import com.direwolf20.buildinggadgets.common.capability.provider.MultiCapabilityProvider;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.concurrent.UndoScheduler;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.save.UndoWorldSave;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.block.Block;
import com.direwolf20.buildinggadgets.common.util.tools.CapabilityUtil.EnergyUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags.Wrapper;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.withSuffix;

public abstract class AbstractGadget extends Item {
    private BaseRenderer renderer;
    private final Tag<Block> whiteList;
    private final Tag<Block> blackList;
    private Supplier<UndoWorldSave> saveSupplier;

    public AbstractGadget(Properties builder, IntSupplier undoLengthSupplier, String undoName, ResourceLocation whiteListTag, ResourceLocation blackListTag) {
        super(builder);
        renderer = DistExecutor.runForDist(this::createRenderFactory, () -> () -> null);
        this.whiteList = new Wrapper(whiteListTag);
        this.blackList = new Wrapper(blackListTag);
        saveSupplier = SaveManager.INSTANCE.registerUndoSave(w -> SaveManager.getUndoSave(w, undoLengthSupplier, undoName));
    }

    public abstract int getEnergyMax();
    public abstract int getEnergyCost(ItemStack tool);

    public Tag<Block> getWhiteList() {
        return whiteList;
    }

    public Tag<Block> getBlackList() {
        return blackList;
    }

    @OnlyIn(Dist.CLIENT)
    public BaseRenderer getRender() {
        return renderer;
    }

    protected abstract Supplier<BaseRenderer> createRenderFactory();

    protected UndoWorldSave getUndoSave() {
        return saveSupplier.get();
    }

    protected void addCapabilityProviders(ImmutableList.Builder<ICapabilityProvider> providerBuilder, ItemStack stack, @Nullable CompoundNBT tag) {
        providerBuilder.add(new CapabilityProviderEnergy(stack, this::getEnergyMax));
        providerBuilder.add(new CapabilityProviderBlockProvider(stack));
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT tag) {
        ImmutableList.Builder<ICapabilityProvider> providerBuilder = ImmutableList.builder();
        addCapabilityProviders(providerBuilder, stack, tag);
        return new MultiCapabilityProvider(providerBuilder.build());
    }

    @Override
    public boolean isDamageable() {
        return getMaxDamage() > 0;
    }

    /*@Override
    public boolean isRepairable() {
        return false;
    }*/

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

    public boolean isAllowedBlock(Block block) {
        if (getWhiteList().getAllElements().isEmpty())
            return ! getBlackList().contains(block);
        return getWhiteList().contains(block);
    }

    public boolean isAllowedBlock(Block block, @Nullable PlayerEntity notifiedPlayer) {
        if (isAllowedBlock(block))
            return true;
        if (notifiedPlayer != null)
            notifiedPlayer.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.invalidblock").getUnformattedComponentText()), true);
        return false;
    }

    public static ItemStack getGadget(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof AbstractGadget)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof AbstractGadget)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public boolean canUse(ItemStack tool, PlayerEntity player) {
        if (player.isCreative())
            return true;

        IEnergyStorage energy = EnergyUtil.getCap(tool).orElseThrow(CapabilityNotPresentException::new);
        return getEnergyCost(tool) <= energy.getEnergyStored();
    }

    public void applyDamage(ItemStack tool, ServerPlayerEntity player) {
        if (player.isCreative())
            return;

        IEnergyStorage energy = EnergyUtil.getCap(tool).orElseThrow(CapabilityNotPresentException::new);
        energy.extractEnergy(getEnergyCost(tool), false);
    }

    protected void addEnergyInformation(List<ITextComponent> tooltip, ItemStack stack) {
        if (Config.isServerConfigLoaded())
            stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
                tooltip.add(TooltipTranslation.GADGET_ENERGY
                                    .componentTranslation(withSuffix(energy.getEnergyStored()), withSuffix(energy.getMaxEnergyStored()))
                                    .setStyle(Styles.WHITE));
            });
    }

    public final void onRotate(ItemStack stack, PlayerEntity player) {
        if (performRotate(stack, player))
            player.sendStatusMessage(MessageTranslation.ROTATED.componentTranslation().setStyle(Styles.AQUA), true);
    }

    protected boolean performRotate(ItemStack stack, PlayerEntity player) {
        return false;
    }

    public final void onMirror(ItemStack stack, PlayerEntity player) {
        if (performMirror(stack, player))
            player.sendStatusMessage(MessageTranslation.MIRRORED.componentTranslation().setStyle(Styles.AQUA), true);
    }

    protected boolean performMirror(ItemStack stack, PlayerEntity player) {
        return false;
    }

    public final void onAnchor(ItemStack stack, PlayerEntity player) {
        if (getAnchor(stack) == null) {
            BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if ((player.world.isAirBlock(lookingAt.getPos())))
                return;
            onAnchorSet(stack, player, lookingAt);
            player.sendStatusMessage(MessageTranslation.ANCHOR_SET.componentTranslation().setStyle(Styles.AQUA), true);
        } else {
            onAnchorRemoved(stack, player);
            player.sendStatusMessage(MessageTranslation.ANCHOR_REMOVED.componentTranslation().setStyle(Styles.AQUA), true);
        }
    }

    protected void onAnchorSet(ItemStack stack, PlayerEntity player, BlockRayTraceResult lookingAt) {
        GadgetUtils.writePOSToNBT(stack, lookingAt.getPos(), NBTKeys.GADGET_ANCHOR);
    }

    protected void onAnchorRemoved(ItemStack stack, PlayerEntity player) {
        NBTHelper.getOrNewTag(stack).remove(NBTKeys.GADGET_ANCHOR);
    }

    @Nullable
    public BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
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

    //this should only be called Server-Side!!!
    public UUID getUUID(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (! nbt.hasUniqueId(NBTKeys.GADGET_UUID)) {
            UUID newId = getUndoSave().getFreeUUID();
            nbt.putUniqueId(NBTKeys.GADGET_UUID, newId);
            return newId;
        }
        return nbt.getUniqueId(NBTKeys.GADGET_UUID);
    }

    protected static String formatName(String name) {
        return name.replaceAll("(?=[A-Z])", " ").trim();
    }

    protected void pushUndo(ItemStack stack, Undo undo) {
        UndoWorldSave save = getUndoSave();
        save.insertUndo(getUUID(stack), undo);
    }

    public void undo(World world, PlayerEntity player, ItemStack stack) {
        UndoWorldSave save = getUndoSave();
        Optional<Undo> undoOptional = save.getUndo(getUUID(stack));
        if (undoOptional.isPresent()) {
            Undo undo = undoOptional.orElseThrow(RuntimeException::new);
            IItemIndex index = InventoryHelper.index(stack, player);
            if (! ForceUnloadedCommand.mayForceUnloadedChunks(player)) {//TODO separate command
                ImmutableSortedSet<ChunkPos> unloadedChunks = undo.getBoundingBox().getUnloadedChunks(world);
                if (! unloadedChunks.isEmpty()) {
                    pushUndo(stack, undo);
                    player.sendStatusMessage(MessageTranslation.UNDO_UNLOADED.componentTranslation().setStyle(Styles.RED), true);
                    BuildingGadgets.LOG.error("Player attempted to undo a Region missing {} unloaded chunks. Denied undo!", unloadedChunks.size());
                    BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", unloadedChunks);
                    return;
                }
            }
            IBuildContext buildContext = SimpleBuildContext.builder()
                    .buildingPlayer(player)
                    .usedStack(stack)
                    .build(world);
            UndoScheduler.scheduleUndo(undo, index, buildContext, Config.GADGETS.placeSteps.get());
        } else
            player.sendStatusMessage(MessageTranslation.NOTHING_TO_UNDO.componentTranslation().setStyle(Styles.RED), true);
    }
}

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
import com.direwolf20.buildinggadgets.common.items.gadgets.modes.*;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.save.UndoWorldSave;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags.Wrapper;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

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
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if( !isInGroup(group) )
            return;

        ItemStack charged = new ItemStack(this);
        charged.getOrCreateTag().putDouble(NBTKeys.ENERGY, this.getEnergyMax());
        items.add(charged);
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
        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if( !cap.isPresent() )
            return super.getDurabilityForDisplay(stack);

        Pair<Integer, Integer> energyStorage = cap.map(e -> Pair.of(e.getEnergyStored(), e.getMaxEnergyStored())).orElse(Pair.of(0, 0));
        return 1D - (energyStorage.getLeft() / (double) energyStorage.getRight());
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if( !cap.isPresent() )
            return super.getRGBDurabilityForDisplay(stack);

        Pair<Integer, Integer> energyStorage = cap.map(e -> Pair.of(e.getEnergyStored(), e.getMaxEnergyStored())).orElse(Pair.of(0, 0));
        return MathHelper.hsvToRGB(Math.max(0.0F, energyStorage.getLeft() / (float) energyStorage.getRight()) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if( !cap.isPresent() )
            return super.isDamaged(stack);

        Pair<Integer, Integer> energyStorage = cap.map(e -> Pair.of(e.getEnergyStored(), e.getMaxEnergyStored())).orElse(Pair.of(0, 0));
        return energyStorage.getLeft() != energyStorage.getRight();
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBTKeys.CREATIVE_MARKER))
            return false;

        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if( !cap.isPresent() )
            return super.showDurabilityBar(stack);

        Pair<Integer, Integer> energyStorage = cap.map(e -> Pair.of(e.getEnergyStored(), e.getMaxEnergyStored())).orElse(Pair.of(0, 0));
        return energyStorage.getLeft() != energyStorage.getRight();
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return !toRepair.getCapability(CapabilityEnergy.ENERGY).isPresent() && repair.getItem() == Items.DIAMOND;
    }

    public boolean isAllowedBlock(Block block) {
        if (getWhiteList().getAllElements().isEmpty())
            return ! getBlackList().contains(block);
        return getWhiteList().contains(block);
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
        if (player.isCreative() || getEnergyMax() == 0)
            return true;

        return getEnergyCost(tool) <= tool.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public void applyDamage(ItemStack tool, ServerPlayerEntity player) {
        if (player.isCreative() || getEnergyMax() == 0)
            return;

        tool.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> e.extractEnergy(getEnergyCost(tool), false));
    }

    protected void addEnergyInformation(List<ITextComponent> tooltip, ItemStack stack) {
        if( getEnergyMax() == 0 )
            return;

        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
            tooltip.add(TooltipTranslation.GADGET_ENERGY
                                .componentTranslation(withSuffix(energy.getEnergyStored()), withSuffix(energy.getMaxEnergyStored()))
                                .setStyle(Styles.GRAY));
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
        stack.getOrCreateTag().remove(NBTKeys.GADGET_ANCHOR);
    }

    @Nullable
    public BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
    }

    public static boolean getFuzzy(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_FUZZY);
    }

    public static void toggleFuzzy(PlayerEntity player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_FUZZY, !getFuzzy(stack));
        player.sendStatusMessage(MessageTranslation.FUZZY_MODE.componentTranslation(getFuzzy(stack)).setStyle(Styles.AQUA), true);
    }

    public static boolean getConnectedArea(ItemStack stack) {
        return !stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_UNCONNECTED_AREA);
    }

    public static void toggleConnectedArea(PlayerEntity player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_UNCONNECTED_AREA, getConnectedArea(stack));
        player.sendStatusMessage((stack.getItem() instanceof GadgetDestruction ? MessageTranslation.CONNECTED_AREA : MessageTranslation.CONNECTED_SURFACE)
                .componentTranslation(getConnectedArea(stack)).setStyle(Styles.AQUA), true);
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_RAYTRACE_FLUID);
    }

    public static void toggleRayTraceFluid(ServerPlayerEntity player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_RAYTRACE_FLUID, !shouldRayTraceFluid(stack));
        player.sendStatusMessage(MessageTranslation.RAYTRACE_FLUID.componentTranslation(shouldRayTraceFluid(stack)).setStyle(Styles.AQUA), true);
    }

    public static void addInformationRayTraceFluid(List<ITextComponent> tooltip, ItemStack stack) {
        tooltip.add(TooltipTranslation.GADGET_RAYTRACE_FLUID
                            .componentTranslation(String.valueOf(shouldRayTraceFluid(stack)))
                            .setStyle(Styles.BLUE));
    }

    //this should only be called Server-Side!!!
    public UUID getUUID(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (! nbt.hasUniqueId(NBTKeys.GADGET_UUID)) {
            UUID newId = getUndoSave().getFreeUUID();
            nbt.putUniqueId(NBTKeys.GADGET_UUID, newId);
            return newId;
        }
        return nbt.getUniqueId(NBTKeys.GADGET_UUID);
    }

    // Todo: tweak and fix.
    public static int getRangeInBlocks(int range, AbstractMode mode) {
        if( mode instanceof StairMode ||
                mode instanceof VerticalColumnMode ||
                mode instanceof HorizontalColumnMode)
            return range;

        if( mode instanceof GridMode)
            return range < 7 ? 9 : range < 13 ? 11 * 11: 19 * 19;

        return range == 1 ? 1 : (range + 1) * (range + 1);
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

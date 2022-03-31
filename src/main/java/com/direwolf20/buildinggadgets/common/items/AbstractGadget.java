package com.direwolf20.buildinggadgets.common.items;


import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.capability.IPrivateEnergy;
import com.direwolf20.buildinggadgets.common.capability.provider.MultiCapabilityProvider;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.modes.*;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.concurrent.UndoScheduler;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.save.SaveManager;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.tainted.save.UndoWorldSave;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
    private final TagKey<Block> whiteList;
    private final TagKey<Block> blackList;
    private Supplier<UndoWorldSave> saveSupplier;

    public AbstractGadget(Properties builder, IntSupplier undoLengthSupplier, String undoName, ResourceLocation whiteListTag, ResourceLocation blackListTag) {
        super(builder.setNoRepair());

        renderer = DistExecutor.runForDist(this::createRenderFactory, () -> () -> null);
        this.whiteList = TagKey.create(Registry.BLOCK_REGISTRY, whiteListTag);
        this.blackList = TagKey.create(Registry.BLOCK_REGISTRY, blackListTag);
        saveSupplier = SaveManager.INSTANCE.registerUndoSave(w -> SaveManager.getUndoSave(w, undoLengthSupplier, undoName));
    }

    public abstract int getEnergyMax();

    public abstract int getEnergyCost(ItemStack tool);

    public TagKey<Block> getWhiteList() {
        return whiteList;
    }

    public TagKey<Block> getBlackList() {
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

    protected void addCapabilityProviders(ImmutableList.Builder<ICapabilityProvider> providerBuilder, ItemStack stack, @Nullable CompoundTag tag) {
        providerBuilder.add(new CapabilityProviderEnergy(stack, this::getEnergyMax));
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag tag) {
        ImmutableList.Builder<ICapabilityProvider> providerBuilder = ImmutableList.builder();
        addCapabilityProviders(providerBuilder, stack, tag);
        return new MultiCapabilityProvider(providerBuilder.build());
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (!allowdedIn(group))
            return;

        ItemStack charged = new ItemStack(this);
        charged.getOrCreateTag().putDouble(NBTKeys.ENERGY, this.getEnergyMax());
        items.add(charged);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if (!cap.isPresent())
            return super.getBarWidth(stack);

        return cap.map(e -> Math.min(13 * e.getEnergyStored() / e.getMaxEnergyStored(), 13))
                .orElse(super.getBarWidth(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if (!cap.isPresent())
            return super.getBarColor(stack);

        Pair<Integer, Integer> energyStorage = cap.map(e -> Pair.of(e.getEnergyStored(), e.getMaxEnergyStored())).orElse(Pair.of(0, 0));
        return Mth.hsvToRgb(Math.max(0.0F, energyStorage.getLeft() / (float) energyStorage.getRight()) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        LazyOptional<IEnergyStorage> cap = stack.getCapability(CapabilityEnergy.ENERGY);
        if (!cap.isPresent())
            return super.isDamaged(stack);

        Pair<Integer, Integer> energyStorage = cap.map(e -> Pair.of(e.getEnergyStored(), e.getMaxEnergyStored())).orElse(Pair.of(0, 0));
        return energyStorage.getLeft() != energyStorage.getRight();
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBTKeys.CREATIVE_MARKER))
            return false;

        return stack.getCapability(CapabilityEnergy.ENERGY).map(e -> e.getEnergyStored() != e.getMaxEnergyStored()).orElse(super.isBarVisible(stack));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return !toRepair.getCapability(CapabilityEnergy.ENERGY).isPresent() && repair.getItem() == Items.DIAMOND;
    }

    public boolean isAllowedBlock(BlockState block) {
        if (Lists.newArrayList(Registry.BLOCK.getTagOrEmpty(getWhiteList())).isEmpty()) {
            return !block.is(getWhiteList());
        }

        return block.is(getWhiteList());
    }

    public static ItemStack getGadget(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof AbstractGadget)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof AbstractGadget)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    public boolean canUse(ItemStack tool, Player player) {
        if (player.isCreative() || getEnergyMax() == 0)
            return true;

        return getEnergyCost(tool) <= tool.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public void applyDamage(ItemStack tool, ServerPlayer player) {
        if (player.isCreative() || getEnergyMax() == 0)
            return;

        tool.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> ((IPrivateEnergy) e).extractPower(getEnergyCost(tool), false));
    }

    protected void addEnergyInformation(List<Component> tooltip, ItemStack stack) {
        if (getEnergyMax() == 0)
            return;

        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
            tooltip.add(TooltipTranslation.GADGET_ENERGY
                    .componentTranslation(withSuffix(energy.getEnergyStored()), withSuffix(energy.getMaxEnergyStored()))
                    .setStyle(Styles.GRAY));
        });
    }

    public final void onRotate(ItemStack stack, Player player) {
        if (performRotate(stack, player))
            player.displayClientMessage(MessageTranslation.ROTATED.componentTranslation().setStyle(Styles.AQUA), true);
    }

    protected boolean performRotate(ItemStack stack, Player player) {
        return false;
    }

    public final void onMirror(ItemStack stack, Player player) {
        if (performMirror(stack, player))
            player.displayClientMessage(MessageTranslation.MIRRORED.componentTranslation().setStyle(Styles.AQUA), true);
    }

    protected boolean performMirror(ItemStack stack, Player player) {
        return false;
    }

    public final void onAnchor(ItemStack stack, Player player) {
        if (getAnchor(stack) == null) {
            BlockHitResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if ((player.level.isEmptyBlock(lookingAt.getBlockPos())))
                return;
            onAnchorSet(stack, player, lookingAt);
            player.displayClientMessage(MessageTranslation.ANCHOR_SET.componentTranslation().setStyle(Styles.AQUA), true);
        } else {
            onAnchorRemoved(stack, player);
            player.displayClientMessage(MessageTranslation.ANCHOR_REMOVED.componentTranslation().setStyle(Styles.AQUA), true);
        }
    }

    protected void onAnchorSet(ItemStack stack, Player player, BlockHitResult lookingAt) {
        GadgetUtils.writePOSToNBT(stack, lookingAt.getBlockPos(), NBTKeys.GADGET_ANCHOR);
    }

    protected void onAnchorRemoved(ItemStack stack, Player player) {
        stack.getOrCreateTag().remove(NBTKeys.GADGET_ANCHOR);
    }

    @Nullable
    public BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
    }

    public static boolean getFuzzy(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_FUZZY);
    }

    public static void toggleFuzzy(Player player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_FUZZY, !getFuzzy(stack));
        player.displayClientMessage(MessageTranslation.FUZZY_MODE.componentTranslation(getFuzzy(stack)).setStyle(Styles.AQUA), true);
    }

    public static boolean getConnectedArea(ItemStack stack) {
        return !stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_UNCONNECTED_AREA);
    }

    public static void toggleConnectedArea(Player player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_UNCONNECTED_AREA, getConnectedArea(stack));
        player.displayClientMessage((stack.getItem() instanceof GadgetDestruction ? MessageTranslation.CONNECTED_AREA : MessageTranslation.CONNECTED_SURFACE)
                .componentTranslation(getConnectedArea(stack)).setStyle(Styles.AQUA), true);
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_RAYTRACE_FLUID);
    }

    public static void toggleRayTraceFluid(ServerPlayer player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_RAYTRACE_FLUID, !shouldRayTraceFluid(stack));
        player.displayClientMessage(MessageTranslation.RAYTRACE_FLUID.componentTranslation(shouldRayTraceFluid(stack)).setStyle(Styles.AQUA), true);
    }

    public static void addInformationRayTraceFluid(List<Component> tooltip, ItemStack stack) {
        tooltip.add(TooltipTranslation.GADGET_RAYTRACE_FLUID
                .componentTranslation(String.valueOf(shouldRayTraceFluid(stack)))
                .setStyle(Styles.BLUE));
    }

    //this should only be called Server-Side!!!
    public UUID getUUID(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.hasUUID(NBTKeys.GADGET_UUID)) {
            UUID newId = getUndoSave().getFreeUUID();
            nbt.putUUID(NBTKeys.GADGET_UUID, newId);
            return newId;
        }
        return nbt.getUUID(NBTKeys.GADGET_UUID);
    }

    // Todo: tweak and fix.
    public static int getRangeInBlocks(int range, AbstractMode mode) {
        if (mode instanceof StairMode ||
                mode instanceof VerticalColumnMode ||
                mode instanceof HorizontalColumnMode)
            return range;

        if (mode instanceof GridMode)
            return range < 7 ? 9 : range < 13 ? 11 * 11 : 19 * 19;

        return range == 1 ? 1 : (range + 1) * (range + 1);
    }

    protected void pushUndo(ItemStack stack, Undo undo) {
        // Don't save if there is nothing to undo...
        if (undo.getUndoData().isEmpty()) {
            return;
        }

        UndoWorldSave save = getUndoSave();
        save.insertUndo(getUUID(stack), undo);
    }

    public void undo(Level world, Player player, ItemStack stack) {
        UndoWorldSave save = getUndoSave();
        Optional<Undo> undoOptional = save.getUndo(getUUID(stack));

        if (undoOptional.isPresent()) {
            Undo undo = undoOptional.get();
            IItemIndex index = InventoryHelper.index(stack, player);
            if (!ForceUnloadedCommand.mayForceUnloadedChunks(player)) {//TODO separate command
                ImmutableSortedSet<ChunkPos> unloadedChunks = undo.getBoundingBox().getUnloadedChunks(world);
                if (!unloadedChunks.isEmpty()) {
                    pushUndo(stack, undo);
                    player.displayClientMessage(MessageTranslation.UNDO_UNLOADED.componentTranslation().setStyle(Styles.RED), true);
                    BuildingGadgets.LOG.error("Player attempted to undo a Region missing {} unloaded chunks. Denied undo!", unloadedChunks.size());
                    BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", unloadedChunks);
                    return;
                }
            }
            BuildContext buildContext = BuildContext.builder()
                    .player(player)
                    .stack(stack)
                    .build(world);

            UndoScheduler.scheduleUndo(undo, index, buildContext, Config.GADGETS.placeSteps.get());
        } else
            player.displayClientMessage(MessageTranslation.NOTHING_TO_UNDO.componentTranslation().setStyle(Styles.RED), true);
    }
}

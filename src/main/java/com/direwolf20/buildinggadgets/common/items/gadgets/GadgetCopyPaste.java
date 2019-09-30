package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.WorldBackedBuildView;
import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionResultExceedsTemplateSizeException;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionResultExceedsTemplateSizeException.BlockPosOutOfBounds;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionResultExceedsTemplateSizeException.ToManyDifferentBlockDataInstances;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.SimpleBuildOpenOptions;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.direwolf20.buildinggadgets.api.template.transaction.ITransactionOperator;
import com.direwolf20.buildinggadgets.api.template.transaction.TemplateTransactions;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock.Mode;
import com.direwolf20.buildinggadgets.common.capability.provider.TemplateKeyProvider;
import com.direwolf20.buildinggadgets.common.commands.CopyUnloadedCommand;
import com.direwolf20.buildinggadgets.common.concurrent.CopyScheduler;
import com.direwolf20.buildinggadgets.common.concurrent.PlacementScheduler;
import com.direwolf20.buildinggadgets.common.concurrent.ServerTickingScheduler;
import com.direwolf20.buildinggadgets.common.concurrent.TransactionPoolExecutor;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.CopyPasteRender;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.save.UndoWorldSave;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.inventory.RecordingItemIndex;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.util.tools.building.PlacementEvaluator;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class GadgetCopyPaste extends AbstractGadget {

    public enum ToolMode {
        COPY(0),
        PASTE(1);
        public static final ToolMode[] VALUES = values();
        private static final Byte2ObjectMap<ToolMode> BY_ID;

        static {
            BY_ID = new Byte2ObjectOpenHashMap<>();
            for (ToolMode mode : VALUES) {
                assert ! BY_ID.containsKey(mode.getId());
                BY_ID.put(mode.getId(), mode);
            }
        }

        private final byte id;

        ToolMode(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return id;
        }

        public ToolMode next() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }

        @Nullable
        public static ToolMode ofId(byte id) {
            return BY_ID.get(id);
        }
    }

    public static final int TRANSACTION_CREATION_LIMIT = 20; //try for one second

    private static final Joiner CHUNK_JOINER = Joiner.on("; ");

    public GadgetCopyPaste(Properties builder) {
        super(builder);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_COPY_PASTE.maxEnergy.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_COPY_PASTE.energyCost.get();
    }

    @Override
    protected Supplier<BaseRenderer> createRenderFactory() {
        return CopyPasteRender::new;
    }

    @Override
    protected UndoWorldSave getUndoSave() {
        return SaveManager.INSTANCE.getCopyPasteUndo();
    }

    @Override
    protected void addCapabilityProviders(Builder<ICapabilityProvider> providerBuilder, ItemStack stack, @Nullable CompoundNBT tag) {
        super.addCapabilityProviders(providerBuilder, stack, tag);
        providerBuilder.add(new TemplateKeyProvider(stack));
    }

    @Override
    public boolean performRotate(ItemStack stack, PlayerEntity player) {
        return performSingleOperator(stack, player, TemplateTransactions.rotateOperator(Rotation.CLOCKWISE_90));
    }

    @Override
    public boolean performMirror(ItemStack stack, PlayerEntity player) {
        return performSingleOperator(stack, player, TemplateTransactions.mirrorOperator(player.getHorizontalFacing().getAxis()));
    }

    private boolean performSingleOperator(ItemStack stack, PlayerEntity player, ITransactionOperator operator) {
        ITemplateProvider provider = player.world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
        ITemplateKey templateKey = stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
        ITemplate template = provider.getTemplateForKey(templateKey);
        ITemplateTransaction transaction = template.startTransaction();
        IBuildContext context = SimpleBuildContext.builder()
                .usedStack(stack)
                .buildingPlayer(player)
                .world(player.world)
                .build();
        if (transaction != null) {
            transaction.operate(operator);
            try {
                transaction.execute(context);
                provider.requestRemoteUpdate(templateKey);
                return true;
            } catch (TransactionExecutionException e) {
                BuildingGadgets.LOG.error("Error whilst executing Rotate/Mirror transaction!", e);
                sendMessage(stack, player, MessageTranslation.TRANSACTION_FAILED, Styles.RED);
                return false;
            }
        } else {
            notifyGadgetBusy(stack, player);
            return false;
        }
    }

    public static int getCopyCounter(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        return nbt.getInt(NBTKeys.TEMPLATE_COPY_COUNT); //returns 0 if not present
    }

    public static int getAndIncrementCopyCounter(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        int count = nbt.getInt(NBTKeys.TEMPLATE_COPY_COUNT); //returns 0 if not present
        nbt.putInt(NBTKeys.TEMPLATE_COPY_COUNT, count + 1);
        return count;
    }

    public static Optional<BlockPos> getActivePos(PlayerEntity playerEntity, ItemStack stack) {
        BlockPos pos = ((AbstractGadget) stack.getItem()).getAnchor(stack);
        if (pos == null) {
            BlockRayTraceResult res = VectorHelper.getLookingAt(playerEntity, stack);
            if (res.getType() == Type.MISS)
                return Optional.empty();
            pos = res.getPos();
        }
        return Optional.of(pos);
    }

    public static Optional<Region> getSelectedRegion(ItemStack stack) {
        BlockPos lower = getLowerRegionBound(stack);
        BlockPos upper = getUpperRegionBound(stack);
        if (lower != null && upper != null)
            return Optional.of(new Region(lower, upper));
        return Optional.empty();
    }

    public static void setSelectedRegion(ItemStack stack, @Nullable Region region) {
        if (region != null) {
            setLowerRegionBound(stack, region.getMin());
            setUpperRegionBound(stack, region.getMax());
        } else {
            setLowerRegionBound(stack, null);
            setUpperRegionBound(stack, null);
        }
    }

    public static void setUpperRegionBound(ItemStack stack, @Nullable BlockPos pos) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (pos != null)
            nbt.put(NBTKeys.GADGET_START_POS, NBTUtil.writeBlockPos(pos));
        else
            nbt.remove(NBTKeys.GADGET_START_POS);
    }

    public static void setLowerRegionBound(ItemStack stack, @Nullable BlockPos pos) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (pos != null)
            nbt.put(NBTKeys.GADGET_END_POS, NBTUtil.writeBlockPos(pos));
        else
            nbt.remove(NBTKeys.GADGET_END_POS);
    }

    @Nullable
    public static BlockPos getUpperRegionBound(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (nbt.contains(NBTKeys.GADGET_START_POS, NBT.TAG_COMPOUND))
            return NBTUtil.readBlockPos(nbt.getCompound(NBTKeys.GADGET_START_POS));
        return null;
    }

    @Nullable
    public static BlockPos getLowerRegionBound(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (nbt.contains(NBTKeys.GADGET_END_POS, NBT.TAG_COMPOUND))
            return NBTUtil.readBlockPos(nbt.getCompound(NBTKeys.GADGET_END_POS));
        return null;
    }

    private static void setLastBuild(ItemStack stack, BlockPos anchorPos, DimensionType dim) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        nbt.put(NBTKeys.GADGET_LAST_BUILD_POS, NBTUtil.writeBlockPos(anchorPos));
        assert dim.getRegistryName() != null;
        nbt.putString(NBTKeys.GADGET_LAST_BUILD_DIM, dim.getRegistryName().toString());
    }

    private static BlockPos getLastBuild(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_LAST_BUILD_POS);
    }

    @Nullable
    private static ResourceLocation getLastBuildDim(ItemStack stack) {
        return GadgetUtils.getDIMFromNBT(stack, NBTKeys.GADGET_LAST_BUILD_DIM);
    }

    private static void setToolMode(ItemStack stack, ToolMode mode) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        tagCompound.putByte(NBTKeys.GADGET_MODE, mode.getId());
    }

    public static ToolMode getToolMode(ItemStack stack) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        ToolMode mode = ToolMode.COPY;
        if (! tagCompound.contains(NBTKeys.GADGET_MODE, NBT.TAG_BYTE)) {
            setToolMode(stack, mode);
            return mode;
        }
        mode = ToolMode.ofId(tagCompound.getByte(NBTKeys.GADGET_MODE));
        if (mode == null) {
            BuildingGadgets.LOG.debug("Failed to read Tool Mode {} falling back to {}.", tagCompound.getString(NBTKeys.GADGET_MODE), mode);
            mode = ToolMode.COPY;
            setToolMode(stack, mode);
        }
        return mode;
    }

    public static ItemStack getGadget(PlayerEntity player) {
        ItemStack stack = AbstractGadget.getGadget(player);
        if (! (stack.getItem() instanceof GadgetCopyPaste))
            return ItemStack.EMPTY;

        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(TooltipTranslation.GADGET_MODE.componentTranslation(getToolMode(stack)).setStyle(Styles.AQUA));
        addEnergyInformation(tooltip, stack);
        addInformationRayTraceFluid(tooltip, stack);
        //EventTooltip.addTemplatePadding(stack, tooltip);
    }

    public void setMode(ItemStack heldItem, int modeInt) {
        // Called when we specify a mode with the radial menu
        ToolMode mode = ToolMode.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        BlockPos posLookingAt = VectorHelper.getPosLookingAt(player, stack);
        // Remove debug code
        // CapabilityUtil.EnergyUtil.getCap(stack).ifPresent(energy -> energy.receiveEnergy(105000, false));
        if (! world.isRemote()) {
            if (player.isSneaking() && GadgetUtils.setRemoteInventory(stack, player, world, posLookingAt, false) == ActionResultType.SUCCESS)
                return new ActionResult<>(ActionResultType.SUCCESS, stack);

            if (getToolMode(stack) == ToolMode.COPY) {
                if (world.getBlockState(posLookingAt) != Blocks.AIR.getDefaultState())
                    setRegionAndCopy(stack, world, player, posLookingAt);
            } else if (getToolMode(stack) == ToolMode.PASTE && ! player.isSneaking()) {
                BlockPos startPos = getAnchor(stack);
                if (startPos == null && ! world.isAirBlock(posLookingAt))
                    startPos = posLookingAt;
                if (startPos != null)
                    build(stack, world, player, posLookingAt);
            }
        } else {
            if (player.isSneaking()) {
                if (Screen.hasControlDown()) {
                    PacketHandler.sendToServer(new PacketBindTool());
                } else if (GadgetUtils.getRemoteInventory(posLookingAt, world, NetworkIO.Operation.EXTRACT) != null)
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            if (getToolMode(stack) == ToolMode.COPY) {
                if (player.isSneaking() && world.getBlockState(posLookingAt) == Blocks.AIR.getDefaultState())
                    GuiMod.COPY.openScreen(player);
            } else if (player.isSneaking()) {
                GuiMod.PASTE.openScreen(player);
            } else {
                BaseRenderer.updateInventoryCache();
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    private void notifyGadgetBusy(ItemStack stack, PlayerEntity player) {
        player.sendStatusMessage(MessageTranslation.GADGET_BUSY.componentTranslation().setStyle(Styles.RED), true);
    }

    private void setRegionAndCopy(ItemStack stack, World world, PlayerEntity player, BlockPos lookedAt) {
        if (player.isSneaking())
            setUpperRegionBound(stack, lookedAt);
        else
            setLowerRegionBound(stack, lookedAt);
        Optional<Region> regionOpt = getSelectedRegion(stack);
        regionOpt.ifPresent(region -> performCopy(stack, world, player, region));
    }

    private void performCopy(ItemStack stack, World world, PlayerEntity player, Region region) {
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                ITemplate template = provider.getTemplateForKey(key);
                if (! CopyUnloadedCommand.mayCopyUnloadedChunks(player)) {
                    ImmutableSortedSet<ChunkPos> unloaded = region.getUnloadedChunks(world);
                    if (! unloaded.isEmpty()) {
                        player.sendStatusMessage(MessageTranslation.COPY_UNLOADED.componentTranslation(unloaded.size()).setStyle(Styles.RED), true);
                        BuildingGadgets.LOG.debug("Prevented copy because {} chunks where detected as unloaded.", unloaded.size());
                        BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", CHUNK_JOINER.join(unloaded));
                        return;
                    }
                }
                SimpleBuildContext context = SimpleBuildContext.builder()
                        .buildingPlayer(player)
                        .usedStack(stack)
                        .build(world);
                WorldBackedBuildView buildView = WorldBackedBuildView.create(context, region);
                runCopyTransaction(stack, template, buildView);
            });

        });
    }

    private void runCopyTransaction(ItemStack stack, ITemplate template, WorldBackedBuildView buildView) {
        IBuildContext context = buildView.getContext();
        assert context.getBuildingPlayer() != null;
        ITemplateTransaction transaction = template.startTransaction();
        if (transaction != null) {
            CopyScheduler.scheduleCopy(map -> {
                transaction
                        .operate(TemplateTransactions.replaceOperator(map))
                        .operate(TemplateTransactions.headerOperator(
                                "Copy " + getAndIncrementCopyCounter(stack),
                                context.getBuildingPlayer().getDisplayName().getUnformattedComponentText()));
                BuildingGadgets.LOG.info("Copying " + map.size());
                if (Config.GADGETS.GADGET_COPY_PASTE.maxSynchronousExecution.get() >= map.size())
                    performTransactionSync(stack, transaction, context);
                else
                    performTransactionAsync(stack, transaction, context);
            }, buildView, Config.GADGETS.GADGET_COPY_PASTE.copySteps.get());
        } else
            notifyGadgetBusy(stack, context.getBuildingPlayer());
    }

    private void performTransactionSync(ItemStack stack, ITemplateTransaction transaction, IBuildContext context) {
        assert context.getBuildingPlayer() != null;
        PlayerEntity player = context.getBuildingPlayer();
        ServerTickingScheduler.runOnServerOnce(() -> {
            try {
                transaction.execute(context);
                onCopyFinished(stack, player);
            } catch (ToManyDifferentBlockDataInstances e) {
                BuildingGadgets.LOG.trace("Too many different Blocks detected!", e);
                onTooManyDifferentBlocks(stack, player);
            } catch (BlockPosOutOfBounds e) {
                BuildingGadgets.LOG.trace("Too large Area detected!", e);
                onTooBigArea(stack, player);
            } catch (TransactionResultExceedsTemplateSizeException e) {
                BuildingGadgets.LOG.trace("Too many Blocks detected!", e);
                onTooManyBlocks(stack, player);
            } catch (TransactionExecutionException e) {
                BuildingGadgets.LOG.error("Transaction Execution failed synchronously!", e);
                onCopyFail(stack, player);
            }
        });
    }

    private void performTransactionAsync(ItemStack stack, ITemplateTransaction transaction, IBuildContext context) {
        assert context.getBuildingPlayer() != null;
        PlayerEntity player = context.getBuildingPlayer();
        if (! TransactionPoolExecutor.INSTANCE.submitTask(() -> {
            try {
                transaction.execute(context);
                ServerTickingScheduler.runOnServerOnce(() -> onCopyFinished(stack, context.getBuildingPlayer()));
            } catch (ToManyDifferentBlockDataInstances e) {
                BuildingGadgets.LOG.trace("Too many different Blocks detected!", e);
                ServerTickingScheduler.runOnServerOnce(() -> onTooManyDifferentBlocks(stack, player));
            } catch (BlockPosOutOfBounds e) {
                BuildingGadgets.LOG.trace("Too large Area detected!", e);
                ServerTickingScheduler.runOnServerOnce(() -> onTooBigArea(stack, player));
            } catch (TransactionResultExceedsTemplateSizeException e) {
                BuildingGadgets.LOG.trace("Too many Blocks detected!", e);
                ServerTickingScheduler.runOnServerOnce(() -> onTooManyBlocks(stack, player));
            } catch (TransactionExecutionException e) {
                BuildingGadgets.LOG.error("Transaction Execution failed asynchronously!", e);
                ServerTickingScheduler.runOnServerOnce(() -> onCopyFail(stack, player));
            }
        })) { //could not submit
            player.sendStatusMessage(MessageTranslation.SERVER_BUSY.componentTranslation().setStyle(Styles.RED), true);
        }
    }

    private void onCopyFinished(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.AREA_COPIED, Styles.DK_GREEN);
        SaveManager.INSTANCE.getTemplateProvider().requestRemoteUpdate(stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY)
                .orElseThrow(CapabilityNotPresentException::new), (ServerPlayerEntity) player);
    }

    private void onTooManyDifferentBlocks(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.AREA_COPIED_FAILED_TOO_MANY_DIFF, Styles.RED);
    }

    private void onTooManyBlocks(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.AREA_COPIED_FAILED_TOO_MANY, Styles.RED);
    }

    private void onTooBigArea(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.AREA_COPIED_FAILED_TOO_BIG, Styles.RED);
    }

    private void onCopyFail(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.AREA_COPIED_FAILED, Styles.RED);
    }

    private void build(ItemStack stack, World world, PlayerEntity player, BlockPos pos) {
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                ITemplate template = provider.getTemplateForKey(key);
                SimpleBuildOpenOptions openOptions = SimpleBuildOpenOptions.withContext(SimpleBuildContext.builder()
                        .usedStack(stack)
                        .buildingPlayer(player)
                        .build(world));
                IBuildView view = template.createViewInContext(openOptions);
                if (view != null)
                    schedulePlacement(stack, view, player, pos);
                else
                    notifyGadgetBusy(stack, player);
            });
        });

    }

    private void schedulePlacement(ItemStack stack, IBuildView view, PlayerEntity player, BlockPos pos) {
        view.translateTo(pos);
        int energyCost = getEnergyCost(stack);
        RecordingItemIndex index = new RecordingItemIndex(InventoryHelper.index(stack, player));
        boolean overwrite = Config.GENERAL.allowOverwriteBlocks.get();
        BlockItemUseContext useContext = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, VectorHelper.getLookingAt(player, stack)));
        PlacementEvaluator evalView = new PlacementEvaluator(
                view,
                stack.getCapability(CapabilityEnergy.ENERGY),
                t -> energyCost,
                index,
                (c, t) -> overwrite ? c.getWorld().getBlockState(t.getPos()).isReplaceable(useContext) : c.getWorld().isAirBlock(t.getPos()),
                true,
                false);
        RegionSnapshot.Recorder recorder = RegionSnapshot.recordAll();
        PlacementScheduler.schedulePlacement(t -> {//TODO PlacementLogic and mechanism to stop when missing blocks!
            recorder.record(view.getContext().getWorld(), t.getPos());
            EffectBlock.spawnEffectBlock(view.getContext(), t, Mode.PLACE, false);
        }, evalView, Config.GADGETS.GADGET_COPY_PASTE.placeSteps.get())
                .withFinisher(() -> {
                    pushUndo(stack, new Undo(
                            recorder.build(view.getContext().getWorld().getDimension().getType()),
                            index.getExtractedItems()));
                    onBuildFinished(stack, player);
                });
    }

    private void onBuildFinished(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.TEMPLATE_BUILD, Styles.DK_GREEN);
    }

    private void sendMessage(ItemStack stack, PlayerEntity player, ITranslationProvider messageSource, Style style) {
        player.sendStatusMessage(messageSource.componentTranslation().setStyle(style), true);
    }
}
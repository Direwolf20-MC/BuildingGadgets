package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.PlacementChecker;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.WorldBackedBuildView;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.capability.provider.TemplateKeyProvider;
import com.direwolf20.buildinggadgets.common.commands.ForceUnloadedCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideBuildSizeCommand;
import com.direwolf20.buildinggadgets.common.commands.OverrideCopySizeCommand;
import com.direwolf20.buildinggadgets.common.concurrent.CopyScheduler;
import com.direwolf20.buildinggadgets.common.concurrent.PlacementScheduler;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.CopyPasteRender;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.template.Template;
import com.direwolf20.buildinggadgets.common.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
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

    public GadgetCopyPaste(Properties builder, IntSupplier undoLengthSupplier, String undoName) {
        super(builder, undoLengthSupplier, undoName);
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
    protected void addCapabilityProviders(Builder<ICapabilityProvider> providerBuilder, ItemStack stack, @Nullable CompoundNBT tag) {
        super.addCapabilityProviders(providerBuilder, stack, tag);
        providerBuilder.add(new TemplateKeyProvider(stack));
    }

    @Override
    public boolean performRotate(ItemStack stack, PlayerEntity player) {
        return player.world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).map(provider ->
                stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).map(key -> {
                    Template template = provider.getTemplateForKey(key);
                    provider.setTemplate(key, template.rotate(Rotation.CLOCKWISE_90));
                    provider.requestRemoteUpdate(key, PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player));
                    return true;
                }).orElse(false))
                .orElse(false);
    }

    @Override
    public boolean performMirror(ItemStack stack, PlayerEntity player) {
        return player.world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).map(provider ->
                stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).map(key -> {
                    Template template = provider.getTemplateForKey(key);
                    provider.setTemplate(key, template.mirror(player.getHorizontalFacing().getAxis()));
                    provider.requestRemoteUpdate(key, PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player));
                    return true;
                }).orElse(false))
                .orElse(false);
    }

    public static void setRelativeVector(ItemStack stack, BlockPos vec) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (vec.equals(BlockPos.ZERO))
            nbt.remove(NBTKeys.GADGET_REL_POS);
        else
            nbt.put(NBTKeys.GADGET_REL_POS, NBTUtil.writeBlockPos(vec));
    }

    public static BlockPos getRelativeVector(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        //if not present, then this will just return (0, 0, 0)
        return NBTUtil.readBlockPos(nbt.getCompound(NBTKeys.GADGET_REL_POS));
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
            pos = res.getPos().offset(res.getFace());
        }
        return Optional.of(pos).map(p -> p.add(getRelativeVector(stack)));
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

    @Override
    protected void onAnchorSet(ItemStack stack, PlayerEntity player, BlockRayTraceResult lookingAt) {
        //offset by one
        super.onAnchorSet(stack, player, new BlockRayTraceResult(lookingAt.getHitVec(), lookingAt.getFace(), lookingAt.getPos().offset(lookingAt.getFace()), lookingAt.isInside()));
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
        GadgetUtils.addTooltipNameAndAuthor(stack, world, tooltip);
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
            } else if (getToolMode(stack) == ToolMode.PASTE && ! player.isSneaking())
                getActivePos(player, stack).ifPresent(pos -> build(stack, world, player, pos));
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
        regionOpt.ifPresent(region -> tryCopy(stack, world, player, region));
    }

    private void tryCopy(ItemStack stack, World world, PlayerEntity player, Region region) {
        if (! checkCopy(world, player, region))
            return;
        SimpleBuildContext context = SimpleBuildContext.builder()
                .buildingPlayer(player)
                .usedStack(stack)
                .build(world);
        WorldBackedBuildView buildView = WorldBackedBuildView.create(context, region,
                (c, p) -> InventoryHelper.getSafeBlockData(player, p, player.getActiveHand()));
        performCopy(stack, buildView);
    }

    private boolean checkCopy(World world, PlayerEntity player, Region region) {
        if (! ForceUnloadedCommand.mayForceUnloadedChunks(player)) {
            ImmutableSortedSet<ChunkPos> unloaded = region.getUnloadedChunks(world);
            if (! unloaded.isEmpty()) {
                player.sendStatusMessage(MessageTranslation.COPY_UNLOADED.componentTranslation(unloaded.size()).setStyle(Styles.RED), true);
                BuildingGadgets.LOG.debug("Prevented copy because {} chunks where detected as unloaded.", unloaded.size());
                BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", CHUNK_JOINER.join(unloaded));
                return false;
            }
        }
        int maxDimension = Config.GADGETS.GADGET_COPY_PASTE.maxCopySize.get();
        if ((region.getXSize() > maxDimension || region.getYSize() > maxDimension || region.getZSize() > maxDimension) &&
                ! OverrideCopySizeCommand.mayPerformLargeCopy(player)) {
            BlockPos sizeVec = region.getMax().subtract(region.getMin());
            player.sendStatusMessage(MessageTranslation.COPY_TOO_LARGE
                    .componentTranslation(sizeVec.getX(), sizeVec.getY(), sizeVec.getZ(), maxDimension, maxDimension, maxDimension)
                    .setStyle(Styles.RED), true);
            return false;
        }
        return true;
    }

    private void performCopy(ItemStack stack, WorldBackedBuildView buildView) {
        IBuildContext context = buildView.getContext();
        assert context.getBuildingPlayer() != null;
        PlayerEntity player = context.getBuildingPlayer();
        CopyScheduler.scheduleCopy((map, region) -> {
            Template newTemplate = new Template(map,
                    TemplateHeader.builder(region)
                            .name("Copy " + getAndIncrementCopyCounter(stack))
                            .author(player.getName().getUnformattedComponentText())
                            .build());
            onCopyFinished(newTemplate.normalize(), stack, player);
        }, buildView, Config.GADGETS.GADGET_COPY_PASTE.copySteps.get());
    }

    private void onCopyFinished(Template newTemplate, ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.AREA_COPIED, Styles.DK_GREEN);
        ITemplateKey key = stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
        SaveManager.INSTANCE.getTemplateProvider().setTemplate(key, newTemplate);
        SaveManager.INSTANCE.getTemplateProvider().requestRemoteUpdate(key, (ServerPlayerEntity) player);
    }

    private void build(ItemStack stack, World world, PlayerEntity player, BlockPos pos) {
        world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                Template template = provider.getTemplateForKey(key);
                IBuildContext buildContext = SimpleBuildContext.builder()
                        .usedStack(stack)
                        .buildingPlayer(player)
                        .build(world);
                IBuildView view = template.createViewInContext(buildContext);
                view.translateTo(pos);
                if (! checkPlacement(world, player, view.getBoundingBox()))
                    return;
                schedulePlacement(stack, view, player);
            });
        });
    }

    private boolean checkPlacement(World world, PlayerEntity player, Region region) {
        if (! ForceUnloadedCommand.mayForceUnloadedChunks(player)) {
            ImmutableSortedSet<ChunkPos> unloaded = region.getUnloadedChunks(world);
            if (! unloaded.isEmpty()) {
                player.sendStatusMessage(MessageTranslation.BUILD_UNLOADED.componentTranslation(unloaded.size()).setStyle(Styles.RED), true);
                BuildingGadgets.LOG.debug("Prevented build because {} chunks where detected as unloaded.", unloaded.size());
                BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", CHUNK_JOINER.join(unloaded));
                return false;
            }
        }
        int maxDimension = Config.GADGETS.GADGET_COPY_PASTE.maxBuildSize.get();
        if ((region.getXSize() > maxDimension || region.getYSize() > maxDimension || region.getZSize() > maxDimension) &&
                ! OverrideBuildSizeCommand.mayPerformLargeBuild(player)) {
            BlockPos sizeVec = region.getMax().subtract(region.getMin());
            player.sendStatusMessage(MessageTranslation.BUILD_TOO_LARGE
                    .componentTranslation(sizeVec.getX(), sizeVec.getY(), sizeVec.getZ(), maxDimension, maxDimension, maxDimension)
                    .setStyle(Styles.RED), true);
            return false;
        }
        return true;
    }

    private void schedulePlacement(ItemStack stack, IBuildView view, PlayerEntity player) {
        IItemIndex index = InventoryHelper.index(stack, player);
        int energyCost = getEnergyCost(stack);
        boolean overwrite = Config.GENERAL.allowOverwriteBlocks.get();
        BlockItemUseContext useContext = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, VectorHelper.getLookingAt(player, stack)));
        PlacementChecker checker = new PlacementChecker(
                stack.getCapability(CapabilityEnergy.ENERGY),
                t -> energyCost,
                index,
                (c, t) -> overwrite ? c.getWorld().getBlockState(t.getPos()).isReplaceable(useContext) : c.getWorld().isAirBlock(t.getPos()),
                true);
        PlacementScheduler.schedulePlacement(view, checker, Config.GADGETS.placeSteps.get())
                .withFinisher(p -> {
                    pushUndo(stack, p.getUndoBuilder().build(view.getContext().getWorld().getDimension().getType()));
                    onBuildFinished(stack, player);
                });
    }

    private void onBuildFinished(ItemStack stack, PlayerEntity player) {
        sendMessage(stack, player, MessageTranslation.TEMPLATE_BUILD, Styles.DK_GREEN);
        onAnchorRemoved(stack, player); //clear the anchor after a successful build
    }

    private void sendMessage(ItemStack stack, PlayerEntity player, ITranslationProvider messageSource, Style style) {
        player.sendStatusMessage(messageSource.componentTranslation().setStyle(style), true);
    }
}
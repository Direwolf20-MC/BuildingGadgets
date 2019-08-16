package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.MapBackedBuildView;
import com.direwolf20.buildinggadgets.api.building.view.WorldBackedBuildView;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.commands.CopyUnloadedCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.CopyPasteRender;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSortedSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;

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

    private static void setAnchor(ItemStack stack, BlockPos anchorPos) {
        GadgetUtils.writePOSToNBT(stack, anchorPos, NBTKeys.GADGET_ANCHOR);
    }

    public static BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
    }

    public static Optional<Region> getSelectedRegion(ItemStack stack) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (nbt.contains(NBTKeys.AREA, NBT.TAG_COMPOUND))
            return Optional.of(Region.deserializeFrom(nbt.getCompound(NBTKeys.AREA)));
        return Optional.empty();
    }

    public static void setSelectedRegion(ItemStack stack, Region region) {
        CompoundNBT nbt = NBTHelper.getOrNewTag(stack);
        if (region != null)
            nbt.put(NBTKeys.AREA, region.serialize());
        else
            nbt.remove(NBTKeys.AREA);
    }

    public static void setUpperRegionBound(ItemStack stack, BlockPos pos) {
        Optional<Region> region = getSelectedRegion(stack); //TODO update when Forge reaches J9
        if (region.isPresent())
            setSelectedRegion(stack, new Region(region.get().getMin(), pos));
        else
            setSelectedRegion(stack, new Region(pos));
    }

    public static void setLowerRegionBound(ItemStack stack, BlockPos pos) {
        Optional<Region> region = getSelectedRegion(stack); //TODO update when Forge reaches J9
        if (region.isPresent())
            setSelectedRegion(stack, new Region(pos, region.get().getMax()));
        else
            setSelectedRegion(stack, new Region(pos));
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
        EventTooltip.addTemplatePadding(stack, tooltip);
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
        if (!world.isRemote) {
            if (player.isSneaking() && GadgetUtils.setRemoteInventory(stack, player, world, posLookingAt, false) == ActionResultType.SUCCESS)
                return new ActionResult<>(ActionResultType.SUCCESS, stack);

            if (getToolMode(stack) == ToolMode.COPY) {
                if (world.getBlockState(posLookingAt) != Blocks.AIR.getDefaultState())
                    setRegionAndCopy(stack, world, player, posLookingAt);
            } else if (getToolMode(stack) == ToolMode.PASTE) {
                if (! player.isSneaking() && player instanceof ServerPlayerEntity) {
                    if (getAnchor(stack) == null) {
                        //if (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) != Blocks.AIR.getDefaultState())
                        //buildBlockMap(world, pos, stack, (ServerPlayerEntity) player);
                    } else {
                        BlockPos startPos = getAnchor(stack);
                        //buildBlockMap(world, startPos, stack, (ServerPlayerEntity) player);
                    }
                }
            }
        } else {
            if (player.isSneaking()) {
                if (Screen.hasControlDown()) {
                    PacketHandler.sendToServer(new PacketBindTool());
                } else {
                    if (GadgetUtils.getRemoteInventory(posLookingAt, world, NetworkIO.Operation.EXTRACT) != null)
                        return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

            }
            if (getToolMode(stack) == ToolMode.COPY) {
                if (player.isSneaking())
                    if (world.getBlockState(posLookingAt) == Blocks.AIR.getDefaultState())
                        GuiMod.COPY.openScreen(player);
            } else if (player.isSneaking()) {
                GuiMod.PASTE.openScreen(player);
            } else {
                BaseRenderer.updateInventoryCache();
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    private void setRegionAndCopy(ItemStack stack, World world, PlayerEntity player, BlockPos lookedAt) {
        if (player.isSneaking())
            setUpperRegionBound(stack, lookedAt);
        else
            setLowerRegionBound(stack, lookedAt);
        performCopy(stack, world, player, lookedAt);
    }

    private void performCopy(ItemStack stack, World world, PlayerEntity player, BlockPos lookedAt) {
        Optional<Region> regOpt = getSelectedRegion(stack);
        Region region = regOpt.orElseThrow(() -> new RuntimeException("Expected Selection Region to be present before copy! This is an internal error, please contact mod authors!"));
        if (! CopyUnloadedCommand.mayCopyUnloadedChunks(player)) {
            ImmutableSortedSet<ChunkPos> unloaded = region.getUnloadedChunks(world);
            if (! unloaded.isEmpty()) {
                BuildingGadgets.LOG.debug("Prevented copy because {} chunks where detected as unloaded.", unloaded.size());
                BuildingGadgets.LOG.trace("The following chunks were detected as unloaded {}.", CHUNK_JOINER.join(unloaded));
                return;
            }
        }
        MapBackedBuildView buildView = WorldBackedBuildView.inWorld(world, region).evaluate();//remember this requires linear time, but is required so that we can pass it off-thread!

    }
}
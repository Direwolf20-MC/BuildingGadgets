package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.building.view.WorldBackedBuildView;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.CopyPasteRender;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.blocks.BlockMap;
import com.direwolf20.buildinggadgets.common.util.blocks.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.EnhancedRuntimeException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        nbt.put(NBTKeys.AREA, region.serialize());
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
        BlockPos pos = VectorHelper.getPosLookingAt(player, stack);
        // Remove debug code
        // CapabilityUtil.EnergyUtil.getCap(stack).ifPresent(energy -> energy.receiveEnergy(105000, false));
        if (!world.isRemote) {
            if (player.isSneaking() && GadgetUtils.setRemoteInventory(stack, player, world, pos, false) == ActionResultType.SUCCESS)
                return new ActionResult<>(ActionResultType.SUCCESS, stack);

            if (getToolMode(stack) == ToolMode.COPY) {
                //if (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) != Blocks.AIR.getDefaultState())
                //this.setPosOrCopy(stack, player, world, pos);
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
                    if (GadgetUtils.getRemoteInventory(pos, world, NetworkIO.Operation.EXTRACT) != null)
                        return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

            }
            if (getToolMode(stack) == ToolMode.COPY) {
                if (player.isSneaking())
                    if (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState())
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
        WorldBackedBuildView buildView = WorldBackedBuildView.inWorld(world, region);
        
    }
}
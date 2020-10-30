package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.items.InventoryWrapper;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.template.Template;
import com.direwolf20.buildinggadgets.common.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GadgetUtils {
    private static final ImmutableList<String> LINK_STARTS = ImmutableList.of("http","www");
    private static final ImmutableList<Block> DISALLOWED_BLOCKS = ImmutableList.of(
            Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.BEDROCK
    );

    public static boolean mightBeLink(final String s) {
        return LINK_STARTS.stream().anyMatch(s::startsWith);
    }

    public static void addTooltipNameAndAuthor(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip) {
        if (world != null) {
            world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
                stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                    Template template = provider.getTemplateForKey(key);
                    TemplateHeader header = template.getHeader();
                    if (header.getName() != null && ! header.getName().isEmpty())
                        tooltip.add(TooltipTranslation.TEMPLATE_NAME.componentTranslation(header.getName()).setStyle(Styles.AQUA));
                    if (header.getAuthor() != null && ! header.getAuthor().isEmpty())
                        tooltip.add(TooltipTranslation.TEMPLATE_AUTHOR.componentTranslation(header.getAuthor()).setStyle(Styles.AQUA));
                });
            });
        }
        EventTooltip.addTemplatePadding(stack, tooltip);
    }

    @Nullable
    public static ByteArrayOutputStream getPasteStream(@Nonnull CompoundNBT compound, @Nullable String name) throws IOException {
        CompoundNBT withText = name != null && !name.isEmpty() ? compound.copy() : compound;
        if (name != null && !name.isEmpty()) withText.putString(NBTKeys.TEMPLATE_NAME, name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(withText, baos);
        return baos.size() < Short.MAX_VALUE - 200 ? baos : null;
    }

    public static void setAnchor(ItemStack stack) {
        setAnchor(stack, new ArrayList<>());
    }

    public static void setAnchor(ItemStack stack, List<BlockPos> coordinates) {
        //Store 1 set of BlockPos in NBT to anchor the Ghost Blocks in the world when the anchor key is pressed
        CompoundNBT tagCompound = stack.getOrCreateTag();
        tagCompound.put(NBTKeys.GADGET_ANCHOR_COORDS, coordinates.stream().map(NBTUtil::writeBlockPos).collect(Collectors.toCollection(ListNBT::new)));
        stack.setTag(tagCompound);
    }

    public static Optional<List<BlockPos>> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null)
            return Optional.empty();

        ListNBT coordList = (ListNBT) tagCompound.get(NBTKeys.GADGET_ANCHOR_COORDS);
        if (coordList == null || coordList.size() == 0)
            return Optional.empty();

        List<BlockPos> coordinates = new ArrayList<>();
        for (int i = 0; i < coordList.size(); i++) {
            coordinates.add(NBTUtil.readBlockPos(coordList.getCompound(i)));
        }

        return Optional.of(coordinates);
    }

    public static void setToolRange(ItemStack stack, int range) {
        //Store the tool's range in NBT as an Integer
        CompoundNBT tagCompound = stack.getOrCreateTag();
        tagCompound.putInt("range", range);
    }

    public static int getToolRange(ItemStack stack) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        return MathHelper.clamp(tagCompound.getInt("range"), 1, 15);
    }

    public static BlockData rotateOrMirrorBlock(PlayerEntity player, PacketRotateMirror.Operation operation, BlockData data) {
        if (operation == PacketRotateMirror.Operation.MIRROR)
            return data.mirror(player.getHorizontalFacing().getAxis() == Axis.X ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK);

        return data.rotate(Rotation.CLOCKWISE_90);
    }

    public static void rotateOrMirrorToolBlock(ItemStack stack, PlayerEntity player, PacketRotateMirror.Operation operation) {
        setToolBlock(stack, rotateOrMirrorBlock(player, operation, getToolBlock(stack)));
        setToolActualBlock(stack, rotateOrMirrorBlock(player, operation, getToolActualBlock(stack)));
    }

    private static void setToolBlock(ItemStack stack, @Nullable BlockData data) {
        //Store the selected block in the tool's NBT
        CompoundNBT tagCompound = stack.getOrCreateTag();
        if (data == null)
            data = BlockData.AIR;

        CompoundNBT stateTag = data.serialize(true);
        tagCompound.put(NBTKeys.TE_CONSTRUCTION_STATE, stateTag);
        stack.setTag(tagCompound);
    }

    private static void setToolActualBlock(ItemStack stack, @Nullable BlockData data) {
        // Store the selected block actual state in the tool's NBT
        CompoundNBT tagCompound = stack.getOrCreateTag();
        if (data == null)
            data = BlockData.AIR;

        CompoundNBT dataTag = data.serialize(true);
        tagCompound.put(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL, dataTag);
        stack.setTag(tagCompound);
    }

    @Nonnull
    public static BlockData getToolBlock(ItemStack stack) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        BlockData res = BlockData.tryDeserialize(tagCompound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE), true);
        if (res == null) {
            setToolActualBlock(stack, BlockData.AIR);
            return BlockData.AIR;
        }
        return res;
    }

    @Nonnull
    public static BlockData getToolActualBlock(ItemStack stack) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        BlockData res = BlockData.tryDeserialize(tagCompound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL), true);
        if (res == null) {
            setToolActualBlock(stack, BlockData.AIR);
            return BlockData.AIR;
        }
        return res;
    }

    public static void bindToolToTE(ItemStack stack, PlayerEntity player) {
        World world = player.world;
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, AbstractGadget.shouldRayTraceFluid(stack) ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
        if (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState())
            return;

        ActionResultType result = setRemoteInventory(stack, player, world, lookingAt.getPos(), true);
    }

    public static ActionResult<Block> selectBlock(ItemStack stack, PlayerEntity player) {
        // Used to find which block the player is looking at, and store it in NBT on the tool.
        World world = player.world;
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, AbstractGadget.shouldRayTraceFluid(stack) ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
        if (world.isAirBlock(lookingAt.getPos()))
            return ActionResult.resultFail(Blocks.AIR);

        BlockState state = world.getBlockState(lookingAt.getPos());
        if (! ((AbstractGadget) stack.getItem()).isAllowedBlock(state.getBlock()) || state.getBlock() instanceof EffectBlock)
            return ActionResult.resultFail(state.getBlock());

        if (DISALLOWED_BLOCKS.contains(state.getBlock())) {
            return ActionResult.resultFail(state.getBlock());
        }

        if (state.getBlockHardness(world, lookingAt.getPos()) < 0) {
            return ActionResult.resultFail(state.getBlock());
        }

        Optional<BlockData> data = InventoryHelper.getSafeBlockData(player, lookingAt.getPos(), player.getActiveHand());
        data.ifPresent(placeState -> {
            BlockState actualState = placeState.getState(); //.getExtendedState(world, lookingAt.getPos()); 1.14 @todo: fix?
            placeState.setState(placeState.getState().getBlock().getDefaultState());

            setToolBlock(stack, placeState);
            setToolActualBlock(stack, new BlockData(actualState, placeState.getTileData()));
        });

        return ActionResult.resultSuccess(state.getBlock());
    }

    public static ActionResultType setRemoteInventory(ItemStack stack, PlayerEntity player, World world, BlockPos pos, boolean setTool) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null)
            return ActionResultType.PASS;

        if (setTool && te instanceof ConstructionBlockTileEntity) {
            ((ConstructionBlockTileEntity) te).getConstructionBlockData();
            setToolBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockData());
            setToolActualBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockData());
            return ActionResultType.SUCCESS;
        }
        if (setRemoteInventory(player, stack, pos, world))
            return ActionResultType.SUCCESS;

        return ActionResultType.FAIL;
    }

    public static boolean anchorBlocks(PlayerEntity player, ItemStack stack) {
        //Stores the current visual blocks in NBT on the tool, so the player can look around without moving the visual render
        Optional<List<BlockPos>> anchorCoords = getAnchor(stack);

        if( anchorCoords.isPresent() ) {  //If theres already an anchor, remove it.
            setAnchor(stack);
            player.sendStatusMessage(MessageTranslation.ANCHOR_REMOVED.componentTranslation().setStyle(Styles.AQUA), true);
            return true;
        }

        //If we don't have an anchor, find the block we're supposed to anchor to
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
        BlockPos startBlock = lookingAt.getPos();
        Direction sideHit = lookingAt.getFace();

        //If we aren't looking at anything, exit
        if (player.world.isAirBlock(startBlock))
            return false;

        BlockData blockData = getToolBlock(stack);
        AbstractMode.UseContext context = new AbstractMode.UseContext(player.world, blockData.getState(), startBlock, stack, sideHit, stack.getItem() instanceof GadgetBuilding && GadgetBuilding.shouldPlaceAtop(stack), stack.getItem() instanceof GadgetBuilding ? GadgetBuilding.getConnectedArea(stack) : GadgetExchanger.getConnectedArea(stack));

        List<BlockPos> coords = stack.getItem() instanceof GadgetBuilding
                ? GadgetBuilding.getToolMode(stack).getMode().getCollection(context, player)
                : GadgetExchanger.getToolMode(stack).getMode().getCollection(context, player);

        setAnchor(stack, coords); //Set the anchor NBT
        player.sendStatusMessage(MessageTranslation.ANCHOR_SET.componentTranslation().setStyle(Styles.AQUA), true);

        return true;
    }

    public static boolean setRemoteInventory(PlayerEntity player, ItemStack tool, BlockPos pos, World world) {
        if (getRemoteInventory(pos, DimensionType.getKey(player.dimension), world) != null) {
            boolean same = pos.equals(getPOSFromNBT(tool, NBTKeys.REMOTE_INVENTORY_POS));
            writePOSToNBT(tool, same ? null : pos, NBTKeys.REMOTE_INVENTORY_POS, player.dimension);
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent(same ? MessageTranslation.UNBOUND_TO_TILE.getTranslationKey() : MessageTranslation.BOUND_TO_TILE.getTranslationKey()).getUnformattedComponentText()), true);
            return true;
        }
        return false;
    }

    @Nullable
    public static IItemHandler getRemoteInventory(ItemStack tool, World world) {
         return getRemoteInventory(tool, world, NetworkIO.Operation.EXTRACT);

    }
    @Nullable
    public static IItemHandler getRemoteInventory(ItemStack tool, World world, NetworkIO.Operation operation) {
        ResourceLocation dim = getDIMFromNBT(tool, NBTKeys.REMOTE_INVENTORY_POS);
        if (dim == null) return null;
        BlockPos pos = getPOSFromNBT(tool, NBTKeys.REMOTE_INVENTORY_POS);
        return pos == null ? null : getRemoteInventory(pos, dim, world /*, operation*/);
    }

    @Nullable
    public static IItemHandler getRemoteInventory(BlockPos pos, ResourceLocation dim, World world) {
        return getRemoteInventory(pos, dim, world, NetworkIO.Operation.EXTRACT);
    }

    @Nullable
    public static IItemHandler getRemoteInventory(BlockPos pos, ResourceLocation dimName, World world, NetworkIO.Operation operation) {
        DimensionType dim = DimensionType.byName(dimName);
        if (dim == null) return null;
        MinecraftServer server = world.getServer();
        if (server == null) return null;
        World worldServer = server.getWorld(dim);

        return getRemoteInventory(pos, worldServer, operation);
    }

    @Nullable
    public static IItemHandler getRemoteInventory(BlockPos pos, World world, NetworkIO.Operation operation) {

        TileEntity te = world.getTileEntity(pos);
        if (te == null) return null;
        //IItemHandler network = RefinedStorage.getWrappedNetwork(te, operation);
        //if (network != null) return network;

        LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if( !cap.isPresent() )
            return null;

        return cap.orElseThrow(CapabilityNotPresentException::new);
    }

    public static String withSuffix(int count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1));
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName) {
        CompoundNBT tagCompound = stack.getOrCreateTag();

        if (pos == null) {
            if (tagCompound.get(tagName) != null) {
                tagCompound.remove(tagName);
                stack.setTag(tagCompound);
            }
            return;
        }
        tagCompound.put(tagName, NBTUtil.writeBlockPos(pos));
        stack.setTag(tagCompound);
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName, DimensionType dimension) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        if (pos == null) {
            tagCompound.get(tagName);
            tagCompound.remove(tagName);
            stack.setTag(tagCompound);
            return;
        }
        CompoundNBT posTag = NBTUtil.writeBlockPos(pos);
        posTag.putString(NBTKeys.GADGET_DIM, DimensionType.getKey(dimension).toString());
        tagCompound.put(tagName, posTag);
        stack.setTag(tagCompound);
    }


    @Nullable
    public static BlockPos getPOSFromNBT(ItemStack stack, String tagName) {
        CompoundNBT stackTag = stack.getOrCreateTag();
        if (! stackTag.contains(tagName))
            return null;
        CompoundNBT posTag = stack.getOrCreateTag().getCompound(tagName);
        if (posTag.isEmpty())
            return null;
        return NBTUtil.readBlockPos(posTag);
    }


    @Nullable
    public static ResourceLocation getDIMFromNBT(ItemStack stack, String tagName) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        CompoundNBT posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new CompoundNBT())) {
            return null;
        }
        return new ResourceLocation(posTag.getString(NBTKeys.GADGET_DIM));
    }

    /**
     * Drops the IItemHandlerModifiable Inventory of the TileEntity at the specified position.
     */
    public static void dropTileEntityInventory(World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            LazyOptional<IItemHandler> cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            cap.ifPresent(handler -> {
                if (handler instanceof IItemHandlerModifiable)
                    net.minecraft.inventory.InventoryHelper.dropInventoryItems(world, pos, new InventoryWrapper((IItemHandlerModifiable) handler));
            });
        }
    }
}
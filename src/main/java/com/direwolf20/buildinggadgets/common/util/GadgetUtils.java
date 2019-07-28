package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.InventoryWrapper;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.util.tools.UndoState;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.tools.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.util.tools.modes.ExchangingMode;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.*;
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

public class GadgetUtils {
    private static final ImmutableList<String> LINK_STARTS = ImmutableList.of("http","www");

    public static boolean mightBeLink(final String s) {
        return LINK_STARTS.stream().anyMatch(s::startsWith);
    }

    public static String getStackErrorSuffix(ItemStack stack) {
        return getStackErrorText(stack) + " with NBT tag: " + stack.getTag();
    }

    private static String getStackErrorText(ItemStack stack) {
        return "the following stack: [" + stack + "]";
    }

    public static CompoundNBT enforceHasTag(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null) {
            nbt = new CompoundNBT();
            stack.setTag(nbt);
        }
        return nbt;
    }

    @Nullable
    public static ByteArrayOutputStream getPasteStream(@Nonnull CompoundNBT compound, @Nullable String name) throws IOException {
        CompoundNBT withText = name != null && !name.isEmpty() ? compound.copy() : compound;
        if (name != null && !name.isEmpty()) withText.putString(NBTKeys.TEMPLATE_NAME, name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(withText, baos);
        return baos.size() < Short.MAX_VALUE - 200 ? baos : null;
    }

    @Nonnull
    public static CompoundNBT getStackTag(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            throw new IllegalArgumentException("An NBT tag could net be retrieved from " + getStackErrorText(stack));

        return tag;
    }

    public static void pushUndoList(ItemStack stack, UndoState undoState) {
        //When we have a new set of Undo Coordinates, push it onto a list stored in NBT, max 10
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        ListNBT undoStates = (ListNBT) tagCompound.get(NBTKeys.GADGET_UNDO_STACK);
        if (undoStates == null) {
            undoStates = new ListNBT();
        }
        if (undoStates.size() >= 10) {
            undoStates.remove(0);
        }
        undoStates.add(undoStateToNBT(undoState));
        tagCompound.put(NBTKeys.GADGET_UNDO_STACK, undoStates);
        stack.setTag(tagCompound);
    }

    @Nullable
    public static UndoState popUndoList(ItemStack stack) {
        //Get the most recent Undo Coordinate set from the list in NBT
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        ListNBT undoStates = (ListNBT) tagCompound.get(NBTKeys.GADGET_UNDO_STACK);
        if (undoStates == null || undoStates.size() == 0) {
            return null;
        }
        UndoState undoState = NBTToUndoState(undoStates.getCompound(undoStates.size() - 1));
        undoStates.remove(undoStates.size() - 1);
        tagCompound.put(NBTKeys.GADGET_UNDO_STACK, undoStates);
        return undoState;
    }

    private static CompoundNBT undoStateToNBT(UndoState undoState) {
        //Convert an UndoState object into NBT data. Uses ints to store relative positions to a start block for data compression..
        CompoundNBT compound = new CompoundNBT();
        compound.putString(NBTKeys.GADGET_DIM, DimensionType.getKey(undoState.dimension).toString());

        BlockPos startBlock = undoState.coordinates.get(0);
        int[] array = new int[undoState.coordinates.size()];
        int idx = 0;
        for (BlockPos coord : undoState.coordinates) {
            //Converts relative blockPos coordinates to a single integer value. Max range 127 due to 8 bits.
            int px = (((coord.getX() - startBlock.getX()) & 0xff) << 16);
            int py = (((coord.getY() - startBlock.getY()) & 0xff) << 8);
            int pz = (((coord.getZ() - startBlock.getZ()) & 0xff));
            int p = (px + py + pz);
            array[idx++] = p;
        }
        compound.put(NBTKeys.GADGET_UNDO_START_POS, NBTUtil.writeBlockPos(startBlock));
        compound.putIntArray(NBTKeys.GADGET_UNDO_INT_COORDS, array);
        return compound;
    }

    @Nullable
    private static UndoState NBTToUndoState(CompoundNBT compound) {
        //Convert an integer list stored in NBT into UndoState
        DimensionType dim = DimensionType.byName(new ResourceLocation(compound.getString(NBTKeys.GADGET_DIM)));
        if (dim == null)
            return null;

        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        int[] array = compound.getIntArray(NBTKeys.GADGET_UNDO_INT_COORDS);
        BlockPos startBlock = NBTUtil.readBlockPos(compound.getCompound(NBTKeys.GADGET_UNDO_START_POS));
        for (int i = 0; i <= array.length - 1; i++) {
            int p = array[i];
            int x = startBlock.getX() + (byte) ((p & 0xff0000) >> 16);
            int y = startBlock.getY() + (byte) ((p & 0x00ff00) >> 8);
            int z = startBlock.getZ() + (byte) (p & 0x0000ff);
            coordinates.add(new BlockPos(x, y, z));
        }

        return new UndoState(dim, coordinates);
    }

    public static void setAnchor(ItemStack stack, List<BlockPos> coordinates) {
        //Store 1 set of BlockPos in NBT to anchor the Ghost Blocks in the world when the anchor key is pressed
        CompoundNBT tagCompound = stack.getTag();
        ListNBT coords = new ListNBT();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        for (BlockPos coord : coordinates) {
            coords.add(NBTUtil.writeBlockPos(coord));
        }
        tagCompound.put(NBTKeys.GADGET_ANCHOR_COORDS, coords);
        stack.setTag(tagCompound);
    }

    public static List<BlockPos> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        CompoundNBT tagCompound = stack.getTag();
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        if (tagCompound == null) {
            setAnchor(stack, coordinates);
            return coordinates;
        }
        ListNBT coordList = (ListNBT) tagCompound.get(NBTKeys.GADGET_ANCHOR_COORDS);
        if (coordList == null) {
            setAnchor(stack, coordinates);
            return coordinates;
        }
        if (coordList.size() == 0) {
            return coordinates;
        }
        for (int i = 0; i < coordList.size(); i++) {
            coordinates.add(NBTUtil.readBlockPos(coordList.getCompound(i)));
        }
        return coordinates;
    }

    public static void setToolRange(ItemStack stack, int range) {
        //Store the tool's range in NBT as an Integer
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        tagCompound.putInt("range", range);
    }

    public static int getToolRange(ItemStack stack) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        return MathHelper.clamp(tagCompound.getInt("range"), 1, 15);
    }

    public static BlockData rotateOrMirrorBlock(PlayerEntity player, PacketRotateMirror.Operation operation, BlockData data) {
        if (operation == PacketRotateMirror.Operation.MIRROR)
            return data.mirror(player.getHorizontalFacing().getAxis() == Axis.X ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK);

        return data.rotate(Rotation.CLOCKWISE_90);
    }

    public static void rotateOrMirrorToolBlock(ItemStack stack, ServerPlayerEntity player, PacketRotateMirror.Operation operation) {
        setToolBlock(stack, rotateOrMirrorBlock(player, operation, getToolBlock(stack)));
        setToolActualBlock(stack, rotateOrMirrorBlock(player, operation, getToolActualBlock(stack)));
    }

    private static void setToolBlock(ItemStack stack, @Nullable BlockData data) {
        //Store the selected block in the tool's NBT
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        if (data == null)
            data = BlockData.AIR;

        CompoundNBT stateTag = data.serialize(true);
        tagCompound.put(NBTKeys.TE_CONSTRUCTION_STATE, stateTag);
        stack.setTag(tagCompound);
    }

    private static void setToolActualBlock(ItemStack stack, @Nullable BlockData data) {
        // Store the selected block actual state in the tool's NBT
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        if (data == null)
            data = BlockData.AIR;

        CompoundNBT dataTag = data.serialize(true);
        tagCompound.put(NBTKeys.TE_CONSTRUCTION_STATE_ACTUAL, dataTag);
        stack.setTag(tagCompound);
    }

    @Nonnull
    public static BlockData getToolBlock(ItemStack stack) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
        BlockData res = BlockData.tryDeserialize(tagCompound.getCompound(NBTKeys.TE_CONSTRUCTION_STATE), true);
        if (res == null) {
            setToolActualBlock(stack, BlockData.AIR);
            return BlockData.AIR;
        }
        return res;
    }

    @Nonnull
    public static BlockData getToolActualBlock(ItemStack stack) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(stack);
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
        if (lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState()))
            return;
        ActionResultType result = setRemoteInventory(stack, player, world, lookingAt.getPos(), true);
    }

    public static void selectBlock(ItemStack stack, PlayerEntity player) {
        // Used to find which block the player is looking at, and store it in NBT on the tool.
        World world = player.world;
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, AbstractGadget.shouldRayTraceFluid(stack) ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
        if (lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState())) return;


        /*ActionResultType result = setRemoteInventory(stack, player, world, lookingAt.getPos(), true);
        if (result == ActionResultType.SUCCESS)
            return;
*/
        BlockState state = world.getBlockState(lookingAt.getPos());
        if (!Config.BLACKLIST.isAllowedBlock(state.getBlock())) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.invalidblock").getUnformattedComponentText()), true);
            return;
        }
        BlockData placeState = InventoryHelper.getSpecificStates(state, world, player, lookingAt.getPos(), stack);
        BlockState actualState = placeState.getState().getExtendedState(world, lookingAt.getPos());

        setToolBlock(stack, placeState);
        setToolActualBlock(stack, new BlockData(actualState, placeState.getTileData()));
    }

    public static ActionResultType setRemoteInventory(ItemStack stack, PlayerEntity player, World world, BlockPos pos, boolean setTool) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null)
            return ActionResultType.PASS;

        if (setTool && te instanceof ConstructionBlockTileEntity && ((ConstructionBlockTileEntity) te).getConstructionBlockData() != null) {
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
        World world = player.world;
        List<BlockPos> currentCoords = getAnchor(stack);
        if (currentCoords.size() == 0) {  //If we don't have an anchor, find the block we're supposed to anchor to
            RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if (lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState())) {  //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = ((BlockRayTraceResult) lookingAt).getPos();
            Direction sideHit = ((BlockRayTraceResult) lookingAt).getFace();
            if (startBlock == null || world.getBlockState(startBlock) == Blocks.AIR.getDefaultState()) { //If we are looking at air, exit
                return false;
            }
            List<BlockPos> coords = new ArrayList<BlockPos>();
            if (stack.getItem() instanceof GadgetBuilding) {
                coords = BuildingMode
                        .collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock); // Build the positions list based on tool mode and range
            } else if (stack.getItem() instanceof GadgetExchanger) {
                coords = ExchangingMode
                        .collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock); // Build the positions list based on tool mode and range
            }
            setAnchor(stack, coords); //Set the anchor NBT
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {  //If theres already an anchor, remove it.
            setAnchor(stack, new ArrayList<BlockPos>());
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
        return true;
    }

    public static boolean setRemoteInventory(PlayerEntity player, ItemStack tool, BlockPos pos, World world) {
        if (getRemoteInventory(pos, DimensionType.getKey(player.dimension), world) != null) {
            boolean same = pos.equals(getPOSFromNBT(tool, NBTKeys.REMOTE_INVENTORY_POS));
            writePOSToNBT(tool, same ? null : pos, NBTKeys.REMOTE_INVENTORY_POS, player.dimension);
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget." + (same ? "unboundTE" : "boundTE")).getUnformattedComponentText()), true);
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
        if (worldServer == null) return null;
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
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
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

    public static void writePOSToNBT(CompoundNBT tagCompound, @Nullable BlockPos pos, String tagName, DimensionType dimension) {
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        if (pos == null) {
            tagCompound.get(tagName);
            tagCompound.remove(tagName);
            return;
        }
        tagCompound.put(tagName, NBTUtil.writeBlockPos(pos));
        tagCompound.putString(NBTKeys.GADGET_DIM, DimensionType.getKey(dimension).toString());
    }

    @Nullable
    public static BlockPos getPOSFromNBT(ItemStack stack, String tagName) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        CompoundNBT posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new CompoundNBT())) {
            return null;
        }
        return NBTUtil.readBlockPos(posTag);
    }

    public static void writeIntToNBT(ItemStack stack, int tagInt, String tagName) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        tagCompound.putInt(tagName, tagInt);
        stack.setTag(tagCompound);
    }

    public static int getIntFromNBT(ItemStack stack, String tagName) {
        CompoundNBT tagCompound = stack.getTag();
        return tagCompound == null ? 0 : tagCompound.getInt(tagName);
    }

    public static void writeStringToNBT(ItemStack stack, String string, String tagName) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        if (string.equals(null)) {
            if (tagCompound.get(tagName) != null) {
                tagCompound.remove(tagName);
            }
            return;
        }
        tagCompound.putString(tagName, string);
    }

    public static void writeStringToNBT(CompoundNBT tagCompound, String string, String tagName) {//TODO unused
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        if (string.equals(null)) {
            if (tagCompound.get(tagName) != null) {
                tagCompound.remove(tagName);
            }
            return;
        }
        tagCompound.putString(tagName, string);
    }

    @Nullable
    public static String getStringFromNBT(ItemStack stack, String tagName) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        return tagCompound.getString(tagName);
    }

    @Nullable
    public static BlockPos getPOSFromNBT(CompoundNBT tagCompound, String tagName) {
        if (tagCompound == null) {
            return null;
        }
        CompoundNBT posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new CompoundNBT())) {
            return null;
        }
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

    public static CompoundNBT stateToCompound(BlockState state) {
        return NBTUtil.writeBlockState(state);
    }

    @Nullable
    public static BlockState compoundToState(@Nullable CompoundNBT tagCompound) {
        if (tagCompound == null) {
            return null;
        }
        return NBTUtil.readBlockState(tagCompound);
    }

    public static int relPosToInt(BlockPos startPos, BlockPos relPos) {
        int px = (((relPos.getX() - startPos.getX()) & 0xff) << 16);
        int py = (((relPos.getY() - startPos.getY()) & 0xff) << 8);
        int pz = (((relPos.getZ() - startPos.getZ()) & 0xff));
        int p = (px + py + pz);
        return p;
    }

    public static BlockPos relIntToPos(BlockPos startPos, int relInt) {
        int p = relInt;
        int x = startPos.getX() + (byte) ((p & 0xff0000) >> 16);
        int y = startPos.getY() + (byte) ((p & 0x00ff00) >> 8);
        int z = startPos.getZ() + (byte) (p & 0x0000ff);
        return new BlockPos(x, y, z);
    }

    public static ListNBT itemCountToNBT(Multiset<UniqueItem> itemCountMap) {
        ListNBT tagList = new ListNBT();

        for (Multiset.Entry<UniqueItem> entry : itemCountMap.entrySet()) {
            int item = Item.getIdFromItem(entry.getElement().getItem());
            int count = entry.getCount();
            CompoundNBT tagCompound = new CompoundNBT();
            tagCompound.putInt(NBTKeys.UNIQUE_ITEM_ITEM, item);
            tagCompound.putInt(NBTKeys.UNIQUE_ITEM_COUNT, count);
            tagList.add(tagCompound);
        }
        return tagList;
    }

    public static Multiset<UniqueItem> nbtToItemCount(@Nullable ListNBT tagList) {
        if (tagList == null) return HashMultiset.create();
        Multiset<UniqueItem> itemCountMap = HashMultiset.create(tagList.size());
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT tagCompound = tagList.getCompound(i);
            UniqueItem uniqueItem = new UniqueItem(Item.getItemById(tagCompound.getInt(NBTKeys.UNIQUE_ITEM_ITEM)));
            int count = tagCompound.getInt(NBTKeys.UNIQUE_ITEM_COUNT);
            itemCountMap.setCount(uniqueItem, count);
        }

        return itemCountMap;
    }

    public static int getItemBurnTime(ItemStack stack) {
        return net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(stack,
                stack.getBurnTime() == - 1 ? AbstractFurnaceTileEntity.getBurnTimes().getOrDefault(stack.getItem(), 0) : stack.getBurnTime());
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
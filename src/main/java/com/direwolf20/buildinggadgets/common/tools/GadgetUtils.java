package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GadgetUtils {

    public static String getStackErrorSuffix(ItemStack stack) {
        return getStackErrorText(stack) + " with NBT tag: " + stack.getTagCompound();
    }

    private static String getStackErrorText(ItemStack stack) {
        return "the following stack: [" + stack + "]";
    }

    @Nullable
    public static ByteArrayOutputStream getPasteStream(@Nonnull NBTTagCompound compound, @Nullable String name) throws IOException{
        NBTTagCompound withText = name != null && !name.isEmpty() ? compound.copy() : compound;
        if (name != null && !name.isEmpty()) withText.setString("name", name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(withText, baos);
        return baos.size() < Short.MAX_VALUE - 200 ? baos : null;
    }

    @Nonnull
    public static NBTTagCompound getStackTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            throw new IllegalArgumentException("An NBT tag could net be retrieved from " + getStackErrorText(stack));

        return tag;
    }

    public static void pushUndoList(ItemStack stack, UndoState undoState) {
        //When we have a new set of Undo Coordinates, push it onto a list stored in NBT, max 10
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList undoStates = (NBTTagList) tagCompound.getTag("undoStack");
        if (undoStates == null) {
            undoStates = new NBTTagList();
        }
        if (undoStates.tagCount() >= 10) {
            undoStates.removeTag(0);
        }
        undoStates.appendTag(undoStateToNBT(undoState));
        tagCompound.setTag("undoStack", undoStates);
        stack.setTagCompound(tagCompound);
    }

    @Nullable
    public static UndoState popUndoList(ItemStack stack) {
        //Get the most recent Undo Coordinate set from the list in NBT
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagList undoStates = (NBTTagList) tagCompound.getTag("undoStack");
        if (undoStates == null || undoStates.tagCount() == 0) {
            return null;
        }
        UndoState undoState = NBTToUndoState(undoStates.getCompoundTagAt(undoStates.tagCount() - 1));
        undoStates.removeTag(undoStates.tagCount() - 1);
        tagCompound.setTag("undoStack", undoStates);
        return undoState;
    }

    private static NBTTagCompound undoStateToNBT(UndoState undoState) {
        //Convert an UndoState object into NBT data. Uses ints to store relative positions to a start block for data compression..
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dim", undoState.dimension);
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
        compound.setTag("startBlock", NBTUtil.createPosTag(startBlock));
        compound.setIntArray("undoIntCoords", array);
        return compound;
    }

    private static UndoState NBTToUndoState(NBTTagCompound compound) {
        //Convert an integer list stored in NBT into UndoState
        int dim = compound.getInteger("dim");
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        int[] array = compound.getIntArray("undoIntCoords");
        BlockPos startBlock = NBTUtil.getPosFromTag(compound.getCompoundTag("startBlock"));
        for (int i = 0; i <= array.length - 1; i++) {
            int p = array[i];
            int x = startBlock.getX() + (byte) ((p & 0xff0000) >> 16);
            int y = startBlock.getY() + (byte) ((p & 0x00ff00) >> 8);
            int z = startBlock.getZ() + (byte) (p & 0x0000ff);
            coordinates.add(new BlockPos(x, y, z));
        }
        UndoState undoState = new UndoState(dim, coordinates);
        return undoState;
    }

    public static void setAnchor(ItemStack stack, List<BlockPos> coordinates) {
        //Store 1 set of BlockPos in NBT to anchor the Ghost Blocks in the world when the anchor key is pressed
        NBTTagCompound tagCompound = stack.getTagCompound();
        NBTTagList coords = new NBTTagList();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        for (BlockPos coord : coordinates) {
            coords.appendTag(NBTUtil.createPosTag(coord));
        }
        tagCompound.setTag("anchorcoords", coords);
        stack.setTagCompound(tagCompound);
    }

    public static List<BlockPos> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        NBTTagCompound tagCompound = stack.getTagCompound();
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        if (tagCompound == null) {
            setAnchor(stack, coordinates);
            tagCompound = stack.getTagCompound();
            return coordinates;
        }
        NBTTagList coordList = (NBTTagList) tagCompound.getTag("anchorcoords");
        if (coordList == null) {
            setAnchor(stack, coordinates);
            tagCompound = stack.getTagCompound();
            return coordinates;
        }
        if (coordList.tagCount() == 0) {
            return coordinates;
        }
        for (int i = 0; i < coordList.tagCount(); i++) {
            coordinates.add(NBTUtil.getPosFromTag(coordList.getCompoundTagAt(i)));
        }
        return coordinates;
    }

    public static void setToolRange(ItemStack stack, int range) {
        //Store the tool's range in NBT as an Integer
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInteger("range", range);
        stack.setTagCompound(tagCompound);
    }

    public static int getToolRange(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null || tagCompound.getInteger("range") == 0) {
            setToolRange(stack, 1);
            tagCompound = stack.getTagCompound();
            return 1;
        }
        return tagCompound.getInteger("range");
    }

    private static void setToolBlock(ItemStack stack, @Nullable IBlockState state) {
        //Store the selected block in the tool's NBT
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }
        NBTTagCompound stateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(stateTag, state);
        tagCompound.setTag("blockstate", stateTag);
        stack.setTagCompound(tagCompound);
    }

    private static void setToolActualBlock(ItemStack stack, @Nullable IBlockState state) {
        //Store the selected block actual state in the tool's NBT
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }
        NBTTagCompound stateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(stateTag, state);
        tagCompound.setTag("actualblockstate", stateTag);
        stack.setTagCompound(tagCompound);
    }


    public static IBlockState getToolBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            setToolBlock(stack, Blocks.AIR.getDefaultState());
            tagCompound = stack.getTagCompound();
            return Blocks.AIR.getDefaultState();
        }
        return NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
    }

    public static IBlockState getToolActualBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            setToolBlock(stack, Blocks.AIR.getDefaultState());
            tagCompound = stack.getTagCompound();
            return Blocks.AIR.getDefaultState();
        }
        return NBTUtil.readBlockState(tagCompound.getCompoundTag("actualblockstate"));
    }

    public static void selectBlock(ItemStack stack, EntityPlayer player) {
        //Used to find which block the player is looking at, and store it in NBT on the tool.
        World world = player.world;
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        if (lookingAt == null)
            return;

        BlockPos pos = lookingAt.getBlockPos();
        EnumActionResult result = setRemoteInventory(stack, player, world, pos, true);
        if (result == EnumActionResult.SUCCESS)
            return;

        IBlockState state = world.getBlockState(pos);
        if (result == EnumActionResult.FAIL || SyncedConfig.blockBlacklist.contains(state.getBlock())) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.invalidblock").getUnformattedComponentText()), true);
            return;
        }
        IBlockState placeState = InventoryManipulation.getSpecificStates(state, world, player, pos, stack);
        IBlockState actualState = placeState.getActualState(world, pos);
        setToolBlock(stack, placeState);
        setToolActualBlock(stack, actualState);
    }

    public static EnumActionResult setRemoteInventory(ItemStack stack, EntityPlayer player, World world, BlockPos pos, boolean setTool) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null)
            return EnumActionResult.PASS;

        if (setTool && te instanceof ConstructionBlockTileEntity && ((ConstructionBlockTileEntity) te).getBlockState() != null) {
            setToolBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
            setToolActualBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
            return EnumActionResult.SUCCESS;
        }
        if (setBoundTE(stack, pos, world.provider.getDimension(), world)) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.boundTE").getUnformattedComponentText()), true);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public static boolean anchorBlocks(EntityPlayer player, ItemStack stack) {
        //Stores the current visual blocks in NBT on the tool, so the player can look around without moving the visual render
        World world = player.world;
        List<BlockPos> currentCoords = getAnchor(stack);
        if (currentCoords.size() == 0) {  //If we don't have an anchor, find the block we're supposed to anchor to
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) {  //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            if (startBlock == null || world.getBlockState(startBlock) == Blocks.AIR.getDefaultState()) { //If we are looking at air, exit
                return false;
            }
            List<BlockPos> coords = new ArrayList<BlockPos>();
            if (stack.getItem() instanceof GadgetBuilding) {
                coords = BuildingModes.getBuildOrders(world, player, startBlock, sideHit, stack); //Build the positions list based on tool mode and range
            } else if (stack.getItem() instanceof GadgetExchanger) {
                coords = ExchangingModes.getBuildOrders(world, player, startBlock, sideHit, stack); //Build the positions list based on tool mode and range
            }
            setAnchor(stack, coords); //Set the anchor NBT
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {  //If theres already an anchor, remove it.
            setAnchor(stack, new ArrayList<BlockPos>());
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
        return true;
    }

    public static boolean setBoundTE(ItemStack tool, @Nullable BlockPos pos, int dim, World world) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null) return false;
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap == null) return false;
        writePOSToNBT(tool, pos, "boundTE", dim);
        return true;
    }

    public static IItemHandler getBoundRemoteInventory(ItemStack tool, World world) {
        Integer dim = getDIMFromNBT(tool, "boundTE");
        if (dim == null) return null;
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return null;
        World worldServer = server.getWorld(dim);
        if (worldServer == null) return null;
        BlockPos pos = getPOSFromNBT(tool, "boundTE");
        if (pos == null) return null;
        TileEntity te = worldServer.getTileEntity(pos);
        if (te == null) return null;
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        return cap == null ? null : cap;
    }

    public static String withSuffix(int count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1));
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        tagCompound.setTag(tagName, NBTUtil.createPosTag(pos));
        stack.setTagCompound(tagCompound);
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName, Integer dim) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        NBTTagCompound posTag = NBTUtil.createPosTag(pos);
        posTag.setInteger("dim", dim);
        tagCompound.setTag(tagName, posTag);
        stack.setTagCompound(tagCompound);
    }

    public static void writePOSToNBT(NBTTagCompound tagCompound, @Nullable BlockPos pos, String tagName, Integer dim) {
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
            }
            return;
        }
        tagCompound.setTag(tagName, NBTUtil.createPosTag(pos));
        tagCompound.setInteger("dim", dim);
    }

    @Nullable
    public static BlockPos getPOSFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompoundTag(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTUtil.getPosFromTag(posTag);
    }

    public static void writeIntToNBT(ItemStack stack, int tagInt, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInteger(tagName, tagInt);
        stack.setTagCompound(tagCompound);
    }

    public static int getIntFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound == null ? 0 : tagCompound.getInteger(tagName);
    }

    public static void writeStringToNBT(ItemStack stack, String string, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (string.equals(null)) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
            }
            return;
        }
        tagCompound.setString(tagName, string);
    }

    public static void writeStringToNBT(NBTTagCompound tagCompound, String string, String tagName) {//TODO unused
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (string.equals(null)) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
            }
            return;
        }
        tagCompound.setString(tagName, string);
    }

    @Nullable
    public static String getStringFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        return tagCompound.getString(tagName);
    }

    @Nullable
    public static BlockPos getPOSFromNBT(NBTTagCompound tagCompound, String tagName) {
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompoundTag(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTUtil.getPosFromTag(posTag);
    }

    @Nullable
    public static Integer getDIMFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompoundTag(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return posTag.getInteger("dim");
    }

    public static NBTTagCompound stateToCompound(IBlockState state) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        NBTUtil.writeBlockState(tagCompound, state);
        return tagCompound;
    }

    @Nullable
    public static IBlockState compoundToState(@Nullable NBTTagCompound tagCompound) {
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

    public static NBTTagList itemCountToNBT(Multiset<UniqueItem> itemCountMap) {
        NBTTagList tagList = new NBTTagList();

        for (Multiset.Entry<UniqueItem> entry : itemCountMap.entrySet()) {
            int item = Item.getIdFromItem(entry.getElement().item);
            int meta = entry.getElement().meta;
            int count = entry.getCount();
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInteger("item", item);
            tagCompound.setInteger("meta", meta);
            tagCompound.setInteger("count", count);
            tagList.appendTag(tagCompound);
        }
        return tagList;
    }

    public static Multiset<UniqueItem> nbtToItemCount(@Nullable NBTTagList tagList) {
        if (tagList == null) return HashMultiset.create();
        Multiset<UniqueItem> itemCountMap = HashMultiset.create(tagList.tagCount());
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            UniqueItem uniqueItem = new UniqueItem(Item.getItemById(tagCompound.getInteger("item")), tagCompound.getInteger("meta"));
            int count = tagCompound.getInteger("count");
            itemCountMap.setCount(uniqueItem, count);
        }

        return itemCountMap;
    }
}
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GadgetUtils {

    public static String getStackErrorSuffix(ItemStack stack) {
        return getStackErrorText(stack) + " with NBT tag: " + stack.getTag();
    }

    private static String getStackErrorText(ItemStack stack) {
        return "the following stack: [" + stack + "]";
    }

    @Nonnull
    public static NBTTagCompound getStackTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTag();
        if (tag == null)
            throw new IllegalArgumentException("An NBT tag could net be retrieved from " + getStackErrorText(stack));

        return tag;
    }

    public static void pushUndoList(ItemStack stack, UndoState undoState) {
        //When we have a new set of Undo Coordinates, push it onto a list stored in NBT, max 10
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList undoStates = (NBTTagList) tagCompound.getTag("undoStack");
        if (undoStates == null) {
            undoStates = new NBTTagList();
        }
        if (undoStates.size() >= 10) {
            undoStates.removeTag(0);
        }
        undoStates.add(undoStateToNBT(undoState));
        tagCompound.setTag("undoStack", undoStates);
        stack.setTag(tagCompound);
    }

    @Nullable
    public static UndoState popUndoList(ItemStack stack) {
        //Get the most recent Undo Coordinate set from the list in NBT
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        NBTTagList undoStates = (NBTTagList) tagCompound.getTag("undoStack");
        if (undoStates == null || undoStates.size() == 0) {
            return null;
        }
        UndoState undoState = NBTToUndoState(undoStates.getCompound(undoStates.size() - 1));
        undoStates.removeTag(undoStates.size() - 1);
        tagCompound.setTag("undoStack", undoStates);
        return undoState;
    }

    private static NBTTagCompound undoStateToNBT(UndoState undoState) {
        //Convert an UndoState object into NBT data. Uses ints to store relative positions to a start block for data compression..
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInt("dim", undoState.dimension);
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
        compound.setTag("startBlock", NBTUtil.writeBlockPos(startBlock));
        compound.setIntArray("undoIntCoords", array);
        return compound;
    }

    private static UndoState NBTToUndoState(NBTTagCompound compound) {
        //Convert an integer list stored in NBT into UndoState
        int dim = compound.getInt("dim");
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        int[] array = compound.getIntArray("undoIntCoords");
        BlockPos startBlock = NBTUtil.readBlockPos(compound.getCompound("startBlock"));
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
        NBTTagCompound tagCompound = stack.getTag();
        NBTTagList coords = new NBTTagList();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        for (BlockPos coord : coordinates) {
            coords.add(NBTUtil.writeBlockPos(coord));
        }
        tagCompound.setTag("anchorcoords", coords);
        stack.setTag(tagCompound);
    }

    public static List<BlockPos> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        NBTTagCompound tagCompound = stack.getTag();
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        if (tagCompound == null) {
            setAnchor(stack, coordinates);
            return coordinates;
        }
        NBTTagList coordList = (NBTTagList) tagCompound.getTag("anchorcoords");
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
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInt("range", range);
        stack.setTag(tagCompound);
    }

    public static int getToolRange(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null || tagCompound.getInt("range") == 0) {
            setToolRange(stack, 1);
            return 1;
        }
        return tagCompound.getInt("range");
    }

    private static void setToolBlock(ItemStack stack, @Nullable IBlockState state) {
        //Store the selected block in the tool's NBT
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }

        NBTTagCompound stateTag = NBTUtil.writeBlockState(state);
        tagCompound.setTag("blockstate", stateTag);
        stack.setTag(tagCompound);
    }

    private static void setToolActualBlock(ItemStack stack, @Nullable IBlockState state) {
        // Store the selected block actual state in the tool's NBT
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }

        NBTTagCompound stateTag = NBTUtil.writeBlockState(state);
        tagCompound.setTag("actualblockstate", stateTag);
        stack.setTag(tagCompound);
    }


    public static IBlockState getToolBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            setToolBlock(stack, Blocks.AIR.getDefaultState());
            return Blocks.AIR.getDefaultState();
        }
        return NBTUtil.readBlockState(tagCompound.getCompound("blockstate"));
    }

    public static IBlockState getToolActualBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            setToolBlock(stack, Blocks.AIR.getDefaultState());
            return Blocks.AIR.getDefaultState();
        }
        return NBTUtil.readBlockState(tagCompound.getCompound("actualblockstate"));
    }

    public static void selectBlock(ItemStack stack, EntityPlayer player) {
        //Used to find which block the player is looking at, and store it in NBT on the tool.
        boolean validBlock = true;
        World world = player.world;
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        if (lookingAt == null) {
            return;
        }
        BlockPos pos = lookingAt.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        if (SyncedConfig.blockBlacklist.contains(state.getBlock())) {
            validBlock = false;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {  //Currently not allowing tile entities and plants.
            if (te instanceof ConstructionBlockTileEntity && ((ConstructionBlockTileEntity) te).getBlockState() != null) {
                setToolBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
                setToolActualBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
                return;
            } else {
                if (!te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent())
                    validBlock = false;
                else {
                    boolean success = setBoundTE(stack, pos, player.dimension, world);
                    if (success) {
                        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.boundTE").getUnformattedComponentText()), true);
                        return;
                    }
                }
            }
            validBlock = false;
        }
        if (!validBlock) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.invalidblock").getUnformattedComponentText()), true);
            return;
        }
        IBlockState placeState = InventoryManipulation.getSpecificStates(state, world, player, pos, stack);
        IBlockState actualState = placeState.getExtendedState(world, pos);

        setToolBlock(stack, placeState);
        setToolActualBlock(stack, actualState);
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

    public static boolean setBoundTE(ItemStack tool, BlockPos pos, int dim, World world) {
        TileEntity te = world.getTileEntity(pos);

        if (te == null)
            return false;

        if (!te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent())
            return false;
        writePOSToNBT(tool, pos, "boundTE", dim);
        return true;
    }

    public static BlockPos getBoundTE(ItemStack tool, World world) {
        BlockPos pos = getPOSFromNBT(tool, "boundTE");
        //BlockPos blankPos = new BlockPos(0,0,0);
        if (pos == null)
            return null;
        TileEntity te = world.getTileEntity(pos);

        if (te == null)
            return null;

        if (!te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent())
            return null;

        return pos;
    }

    public static String withSuffix(int count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1));
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            if (tagCompound.getTag(tagName) != null) {
                tagCompound.removeTag(tagName);
                stack.setTag(tagCompound);
            }
            return;
        }
        tagCompound.setTag(tagName, NBTUtil.writeBlockPos(pos));
        stack.setTag(tagCompound);
    }

    public static void writePOSToNBT(ItemStack stack, @Nullable BlockPos pos, String tagName, Integer dim) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            tagCompound.getTag(tagName);
            tagCompound.removeTag(tagName);
            stack.setTag(tagCompound);
            return;
        }
        NBTTagCompound posTag = NBTUtil.writeBlockPos(pos);
        posTag.setInt("dim", dim);
        tagCompound.setTag(tagName, posTag);
        stack.setTag(tagCompound);
    }

    public static void writePOSToNBT(NBTTagCompound tagCompound, @Nullable BlockPos pos, String tagName, Integer dim) {
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (pos == null) {
            tagCompound.getTag(tagName);
            tagCompound.removeTag(tagName);
            return;
        }
        tagCompound.setTag(tagName, NBTUtil.writeBlockPos(pos));
        tagCompound.setInt("dim", dim);
    }

    @Nullable
    public static BlockPos getPOSFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTUtil.readBlockPos(posTag);
    }

    public static void writeIntToNBT(ItemStack stack, int tagInt, String tagName) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInt(tagName, tagInt);
        stack.setTag(tagCompound);
    }

    public static int getIntFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTag();
        return tagCompound == null ? 0 : tagCompound.getInt(tagName);
    }

    public static void writeStringToNBT(ItemStack stack, String string, String tagName) {
        NBTTagCompound tagCompound = stack.getTag();
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
        NBTTagCompound tagCompound = stack.getTag();
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
        NBTTagCompound posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTUtil.readBlockPos(posTag);
    }

    @Nullable
    public static Integer getDIMFromNBT(ItemStack stack, String tagName) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound posTag = tagCompound.getCompound(tagName);
        if (posTag.equals(new NBTTagCompound())) {
            return null;
        }
        return posTag.getInt("dim");
    }

    public static NBTTagCompound stateToCompound(IBlockState state) {
        return NBTUtil.writeBlockState(state);
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
            int count = entry.getCount();
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setInt("item", item);
            tagCompound.setInt("count", count);
            tagList.add(tagCompound);
        }
        return tagList;
    }

    public static Multiset<UniqueItem> nbtToItemCount(@Nullable NBTTagList tagList) {
        if (tagList == null) return HashMultiset.create();
        Multiset<UniqueItem> itemCountMap = HashMultiset.create(tagList.size());
        for (int i = 0; i < tagList.size(); i++) {
            NBTTagCompound tagCompound = tagList.getCompound(i);
            UniqueItem uniqueItem = new UniqueItem(Item.getItemById(tagCompound.getInt("item")));
            int count = tagCompound.getInt("count");
            itemCountMap.setCount(uniqueItem, count);
        }

        return itemCountMap;
    }
}
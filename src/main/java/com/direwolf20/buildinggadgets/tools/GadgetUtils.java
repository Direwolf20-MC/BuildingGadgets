package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;

public class GadgetUtils {

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

    public static NBTTagCompound undoStateToNBT(UndoState undoState) {
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

    public static UndoState NBTToUndoState(NBTTagCompound compound) {
        //Convert an integer list stored in NBT into UndoState
        int dim = compound.getInteger("dim");
        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        int[] array = compound.getIntArray("undoIntCoords");
        BlockPos startBlock = NBTUtil.getPosFromTag(compound.getCompoundTag("startBlock"));
        for (int i = 0; i <= array.length - 1; i++) {
            int p = array[i];
            int x = startBlock.getX() + (int) (byte) ((p & 0xff0000) >> 16);
            int y = startBlock.getY() + (int) (byte) ((p & 0x00ff00) >> 8);
            int z = startBlock.getZ() + (int) (byte) (p & 0x0000ff);
            coordinates.add(new BlockPos(x, y, z));
        }
        UndoState undoState = new UndoState(dim, coordinates);
        return undoState;
    }

    public static void setAnchor(ItemStack stack, ArrayList<BlockPos> coordinates) {
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

    public static ArrayList<BlockPos> getAnchor(ItemStack stack) {
        //Return the list of coordinates in the NBT Tag for anchor Coordinates
        NBTTagCompound tagCompound = stack.getTagCompound();
        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        NBTTagList coordList = (NBTTagList) tagCompound.getTag("anchorcoords");
        if (tagCompound == null || coordList == null) {
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

    public static void setToolBlock(ItemStack stack, IBlockState state) {
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

    public static void setToolActualBlock(ItemStack stack, IBlockState state) {
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
        if (lookingAt == null) {
            return;
        }
        BlockPos pos = lookingAt.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {  //Currently not allowing tile entities and plants.
            if (te instanceof ConstructionBlockTileEntity) {
                if (((ConstructionBlockTileEntity) te).getBlockState() != null) {
                    setToolBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
                    setToolActualBlock(stack, ((ConstructionBlockTileEntity) te).getActualBlockState());
                    return;
                } else {
                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.invalidblock").getUnformattedComponentText()), true);
                    return;
                }
            } else {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.invalidblock").getUnformattedComponentText()), true);
                return;
            }
        }
        if (state != null) {
            IBlockState placeState = InventoryManipulation.getSpecificStates(state, world, player, pos);
            IBlockState actualState = placeState.getBlock().getActualState(placeState, world, pos);
            setToolBlock(stack, placeState);
            setToolActualBlock(stack, actualState);
        }
    }

    public static boolean anchorBlocks(EntityPlayer player, ItemStack stack) {
        //Stores the current visual blocks in NBT on the tool, so the player can look around without moving the visual render
        World world = player.world;
        ArrayList<BlockPos> currentCoords = getAnchor(stack);
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
            ArrayList<BlockPos> coords = new ArrayList<BlockPos>();
            if (stack.getItem() instanceof BuildingTool) {
                coords = BuildingModes.getBuildOrders(world, player, startBlock, sideHit, stack); //Build the positions list based on tool mode and range
            } else if (stack.getItem() instanceof ExchangerTool) {
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

    public static boolean useEnergy(ItemStack stack, int amount) {
        IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (amount > energy.getEnergyStored()) {
            return false;
        } else {
            energy.extractEnergy(amount, false);
            return true;
        }
    }

    public static String withSuffix(int count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp - 1));
    }
}
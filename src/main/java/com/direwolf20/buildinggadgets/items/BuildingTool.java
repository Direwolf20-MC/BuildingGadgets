package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.tools.BuildingModes;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.UndoState;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BuildingTool extends Item {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public enum toolModes {
        BuildToMe, VerticalColumn, HorizontalColumn, VerticalWall, HorizontalWall;
        private static toolModes[] vals = values();

        public toolModes next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public BuildingTool() {
        setRegistryName("buildingtool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".buildingtool");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public static NBTTagCompound initToolTag(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
            tagCompound.setString("mode", toolModes.BuildToMe.name());
            tagCompound.setInteger("range", 1);
            stack.setTagCompound(tagCompound);
            NBTTagCompound stateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(stateTag, Blocks.AIR.getDefaultState());
            tagCompound.setTag("blockstate", stateTag);
            NBTTagList coords = new NBTTagList();
            tagCompound.setTag("anchorcoords", coords);
        }
        return tagCompound;
    }

    public static void pushUndoList(ItemStack stack, UndoState undoState) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
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
    }

    public static UndoState popUndoList(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
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
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("dim", undoState.dimension);
        BlockPos startBlock = undoState.coordinates.get(0);
        int[] array = new int[undoState.coordinates.size()];
        int idx = 0;
        for (BlockPos coord : undoState.coordinates) {
            int px = (((coord.getX() - startBlock.getX()) & 0xff) <<16);
            int py = (((coord.getY() - startBlock.getY()) & 0xff) <<8);
            int pz = (((coord.getZ() - startBlock.getZ()) & 0xff));
            int p = px+py+pz;
            array[idx++] = p;
        }
        compound.setTag("startBlock",NBTUtil.createPosTag(startBlock));
        compound.setIntArray("undoIntCoords",array);
        return compound;
    }

    public static UndoState NBTToUndoState(NBTTagCompound compound) {
        int dim = compound.getInteger("dim");
        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        int[] array = compound.getIntArray("undoIntCoords");
        BlockPos startBlock = NBTUtil.getPosFromTag(compound.getCompoundTag("startBlock"));
        for (int i = 0; i <= array.length - 1; i++) {
            int p = array[i];
            int x = startBlock.getX() + (int)(byte)(p>>16);
            int y = startBlock.getY() + (int)(byte)((p-(x<<16))>>8);
            int z = startBlock.getZ() + (int)(byte)(p-(x<<16)-(y<<8));
            coordinates.add(new BlockPos(x,y,z));
        }
        UndoState undoState = new UndoState(dim, coordinates);
        return undoState;
    }

    public static void setAnchor(ItemStack stack, ArrayList<BlockPos> coordinates) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        NBTTagList coords = new NBTTagList();
        for (BlockPos coord : coordinates) {
            coords.appendTag(NBTUtil.createPosTag(coord));
        }
        tagCompound.setTag("anchorcoords", coords);
    }

    public static ArrayList<BlockPos> getAnchor(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        NBTTagList coordList = (NBTTagList) tagCompound.getTag("anchorcoords");
        if (coordList.tagCount() == 0 || coordList == null) {
            return coordinates;
        }

        for (int i = 0; i < coordList.tagCount(); i++) {
            coordinates.add(NBTUtil.getPosFromTag(coordList.getCompoundTagAt(i)));
        }
        return coordinates;
    }

    public static void setToolMode(ItemStack stack, toolModes mode) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        tagCompound.setString("mode", mode.name());
    }

    public static void setToolRange(ItemStack stack, int range) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        tagCompound.setInteger("range", range);
    }

    public static void setToolBlock(ItemStack stack, IBlockState state) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }
        NBTTagCompound stateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(stateTag, state);
        tagCompound.setTag("blockstate", stateTag);
    }

    public static toolModes getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }

        return toolModes.valueOf(tagCompound.getString("mode"));
    }

    public static int getToolRange(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        return tagCompound.getInteger("range");
    }

    public static IBlockState getToolBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        return NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
    }


    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, player, list, b);
        list.add(TextFormatting.DARK_GREEN + "Block: " + getToolBlock(stack).getBlock().getLocalizedName());
        list.add(TextFormatting.AQUA + "Mode: " + getToolMode(stack));
        if (getToolMode(stack) != toolModes.BuildToMe) {
            list.add(TextFormatting.RED + "Range: " + getToolRange(stack));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(stack, player);
            } else {
                build(player, stack);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        NBTTagCompound tagCompound = itemstack.getTagCompound();
        ByteBuf buf = Unpooled.buffer(16);
        ByteBufUtils.writeTag(buf,tagCompound);
        System.out.println(buf.readableBytes());
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(itemstack, player);
            } else {
                build(player, itemstack);
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    private void selectBlock(ItemStack stack, EntityPlayer player) {
        World world = player.world;
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        if (lookingAt == null) {
            return;
        }
        BlockPos pos = lookingAt.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Invalid Block"), true);
            return;
        }
        if (state != null) {
            setToolBlock(stack, state);
        }
    }

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {
        toolModes mode = getToolMode(heldItem);
        mode = mode.next();
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + "Tool Mode: " + mode.name()), true);
    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        if (player.isSneaking()) {
            if (range == 1) {
                range = Config.maxRange;
            } else {
                range--;
            }
        } else {
            if (range >= Config.maxRange) {
                range = 1;
            } else {
                range++;
            }
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_BLUE + "Tool range: " + range), true);
    }

    public boolean anchorBlocks(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        int range = getToolRange(stack);
        toolModes mode = getToolMode(stack);
        ArrayList<BlockPos> currentCoords = getAnchor(stack);

        if (currentCoords.size() == 0) {
            //If we don't have an anchor, find the block we're supposed to anchor to
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            //If we aren't looking at anything, exit
            if (lookingAt == null) {
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            //If we are looking at air, exit
            if (startBlock == null || world.getBlockState(startBlock) == Blocks.AIR.getDefaultState()) {
                return false;
            }
            //Build the positions list based on tool mode and range
            ArrayList<BlockPos> coords = BuildingModes.getBuildOrders(world, player, startBlock, sideHit, range, mode, getToolBlock(stack));
            //Set the anchor NBT
            setAnchor(stack, coords);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + "Render Anchored"), true);
        } else {
            //If theres already an anchor, remove it.
            setAnchor(stack, new ArrayList<BlockPos>());
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + "Anchor Removed"), true);
        }
        return true;
    }

    public boolean build(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        int range = getToolRange(stack);
        toolModes mode = getToolMode(stack);
        ArrayList<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) {
            //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            //If we aren't looking at anything, exit
            if (lookingAt == null) {
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            coords = BuildingModes.getBuildOrders(world, player, startBlock, sideHit, range, mode, getToolBlock(stack));
        } else {
            //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        ArrayList<BlockPos> undoCoords = new ArrayList<BlockPos>();
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);
        ItemStack heldItem = player.getHeldItemMainhand();
        IBlockState blockState = getToolBlock(heldItem);

        if (blockState != Blocks.AIR.getDefaultState()) {
            //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            IBlockState state = Blocks.AIR.getDefaultState(); //Initialize a new State Variable for use in the fake world
            fakeWorld.setWorldAndState(player.world, blockState, coordinates); // Initialize the fake world's blocks

            for (BlockPos coordinate : coords) {
                if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try {
                        //Get the state of the block in the fake world (This lets fences be connected, etc)
                        state = blockState.getActualState(fakeWorld, coordinate);
                    } catch (Exception var8) {
                    }
                }
                //Get the extended block state in the fake world
                //Disabled to fix Chisel
                //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                if (placeBlock(world, player, coordinate, state)) {
                    //If we successfully place the block, add the location to the undo list.
                    undoCoords.add(coordinate);
                }
            }
            if (undoCoords.size() > 0) {
                //If the undo list has any data in it, add it to the player Map.
                UndoState undoState = new UndoState(player.dimension, undoCoords);
                //Stack<UndoState> undoStack = UndoBuild.getPlayerMap(player.getUniqueID());
                //undoStack.push(undoState);
                //UndoBuild.updatePlayerMap(player.getUniqueID(), undoStack);
                pushUndoList(heldItem, undoState);
            }
        }
        BuildingModes.sortByDistance(coords, player);
        return true;
    }

    public static boolean undoBuild(EntityPlayer player) {
        //Stack<UndoState> undoStack = UndoBuild.getPlayerMap(player.getUniqueID());
        ItemStack heldItem = player.getHeldItemMainhand();
        UndoState undoState = popUndoList(heldItem);
        if (undoState == null) {
            return false;
        } //Exit if theres nothing to undo.
        World world = player.world;
        if (!world.isRemote) {
            IBlockState currentBlock = Blocks.AIR.getDefaultState();
            //UndoState undoState = undoStack.pop(); //Get the Dim and Coords to Undo
            ArrayList<BlockPos> undoCoords = undoState.coordinates; //Get the Coords to undo
            int dimension = undoState.dimension; //Get the Dimension to undo
            ArrayList<BlockPos> failedRemovals = new ArrayList<BlockPos>();
            for (BlockPos coord : undoCoords) {
                currentBlock = world.getBlockState(coord);
                ItemStack itemStack = currentBlock.getBlock().getPickBlock(currentBlock, null, world, coord, player);
                //Only allow undo from the same dimension, if you're within 35 blocks (Due to chunkloading concerns)
                double distance = coord.getDistance(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
                boolean sameDim = (player.dimension == dimension);
                if (distance < 35 && sameDim && currentBlock != ModBlocks.effectBlock.getDefaultState()) { //Don't allow us to undo a block while its still being placed
                    if (InventoryManipulation.giveItem(itemStack, player)) {
                        //Try to give the player an item, if inventory is full this fails
                        world.spawnEntity(new BlockBuildEntity(world, coord, player, currentBlock, 2));
                    } else {
                        //If we failed to give the item, we want to put this back on the undo list, so start building a list
                        failedRemovals.add(coord);
                    }
                } else {
                    //If you're in the wrong dimension or too far away, fail the undo.
                    failedRemovals.add(coord);
                }
            }
            if (failedRemovals.size() != 0) {
                //Add any failed undo blocks to the undo stack.
                UndoState failedState = new UndoState(player.dimension, failedRemovals);
                pushUndoList(heldItem, failedState);
                //undoStack.push(failedState);
                //UndoBuild.updatePlayerMap(player.getUniqueID(), undoStack);
            }
        }
        return true;
    }

    public static boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        ItemStack itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        if (player.isSpectator()) {
            return false;
        }
        //if (!player.canPlayerEdit(pos,EnumFacing.UP,player.getHeldItemMainhand())) {return false;}
        if (InventoryManipulation.useItem(itemStack, player)) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 1));
            return true;
        }
        return false;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }

}

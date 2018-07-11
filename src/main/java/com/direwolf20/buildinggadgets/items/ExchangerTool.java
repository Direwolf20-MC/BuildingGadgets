package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.tools.ExchangingModes;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExchangerTool extends Item {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public enum toolModes {
        Wall, VerticalColumn, HorizontalColumn;
        private static ExchangerTool.toolModes[] vals = values();

        public ExchangerTool.toolModes next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public ExchangerTool() {
        setRegistryName("exchangertool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".exchangertool");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public int getItemEnchantability() {
        return 3;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        if (EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SILK_TOUCH)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) {
            return true;
        }
        return false;
    }

    public static NBTTagCompound initToolTag(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
            tagCompound.setString("mode", ExchangerTool.toolModes.Wall.name());
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

    public static void setToolMode(ItemStack stack, ExchangerTool.toolModes mode) {
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

    public static ExchangerTool.toolModes getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = initToolTag(stack);
        }
        if (tagCompound.getString("mode") != "") {
            return ExchangerTool.toolModes.valueOf(tagCompound.getString("mode"));
        } else {
            return toolModes.Wall;
        }
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
        list.add(TextFormatting.RED + "Range: " + getToolRange(stack));
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(stack, player);
            } else {
                exchange(player, stack);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(itemstack, player);
            } else {
                exchange(player, itemstack);
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
        ExchangerTool.toolModes mode = getToolMode(heldItem);
        mode = mode.next();
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + "Tool Mode: " + mode.name()), true);
    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        if (player.isSneaking()) {
            if (range <= 1) {
                range = Config.maxRange;
            } else {
                range = range - 2;
            }
        } else {
            if (range >= Config.maxRange) {
                range = 1;
            } else {
                range = range + 2;
            }
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_BLUE + "Tool range: " + range), true);
    }

    public boolean anchorBlocks(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        int range = getToolRange(stack);
        ExchangerTool.toolModes mode = getToolMode(stack);
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
            IBlockState setBlock = getToolBlock(stack);
            //If we are looking at air, exit
            if (startBlock == null || world.getBlockState(startBlock) == Blocks.AIR.getDefaultState()) {
                return false;
            }
            //Build the positions list based on tool mode and range
            ArrayList<BlockPos> coords = ExchangingModes.getBuildOrders(world, player, startBlock, sideHit, range, mode, setBlock);
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

    public boolean exchange(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        int range = getToolRange(stack);
        ExchangerTool.toolModes mode = getToolMode(stack);
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
            IBlockState setBlock = getToolBlock(stack);
            coords = ExchangingModes.getBuildOrders(world, player, startBlock, sideHit, range, mode, setBlock);
        } else {
            //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);
        ItemStack heldItem = player.getHeldItemMainhand();
        IBlockState blockState = getToolBlock(heldItem);

        if (blockState != Blocks.AIR.getDefaultState()) {  //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
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
                exchangeBlock(world, player, coordinate, state);
            }
        }
        return true;
    }

    public boolean exchangeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        IBlockState currentBlock = world.getBlockState(pos);
        ItemStack itemStack = getSilkTouchDrop(setBlock);
        ItemStack tool = player.getHeldItemMainhand();
        if (InventoryManipulation.countItem(itemStack, player) == 0) {
            return false;
        }
        currentBlock.getBlock().harvestBlock(world,player,pos,currentBlock,world.getTileEntity(pos),tool);

        InventoryManipulation.useItem(itemStack, player);
        world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 3));
        return true;
    }

    public static ItemStack getSilkTouchDrop(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        int i = 0;

        if (item.getHasSubtypes()) {
            i = state.getBlock().getMetaFromState(state);
        }
        return new ItemStack(item, 1, i);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

}

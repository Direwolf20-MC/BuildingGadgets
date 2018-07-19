package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.tools.ExchangingModes;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExchangerTool extends Item {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public enum toolModes {
        Wall, VerticalColumn, HorizontalColumn, Checkerboard;
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
        }
        if (tagCompound.getTag("mode") == null) {
            tagCompound.setString("mode", ExchangerTool.toolModes.Wall.name());
        }
        if (tagCompound.getTag("range") == null) {
            tagCompound.setInteger("range", 1);
        }
        if (tagCompound.getCompoundTag("blockstate").getSize() == 0) {
            NBTTagCompound stateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(stateTag, Blocks.AIR.getDefaultState());
            tagCompound.setTag("blockstate", stateTag);
        }
        if (tagCompound.getTag("coords") == null) {
            NBTTagList coords = new NBTTagList();
            tagCompound.setTag("anchorcoords", coords);
        }
        stack.setTagCompound(tagCompound);
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
        list.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.gadget.block") + ": " + getToolBlock(stack).getBlock().getLocalizedName());
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));
        list.add(TextFormatting.RED + I18n.format("tooltip.gadget.range") + ": " + getToolRange(stack));
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
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.invalidblock").getUnformattedComponentText()), true);
            return;
        }
        if (state != null) {
            IBlockState placeState = InventoryManipulation.getSpecificStates(state, world, player, pos);
            setToolBlock(stack, placeState);
        }
    }

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {
        ExchangerTool.toolModes mode = getToolMode(heldItem);
        mode = mode.next();
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode.name()), true);
    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        int changeAmount = getToolMode(heldItem) == toolModes.Checkerboard ? 1 : 2;
        if (player.isSneaking()) {
            if (range <= 1) {
                range = Config.maxRange;
            } else {

                range = range - changeAmount;
            }
        } else {
            if (range >= Config.maxRange) {
                range = 1;
            } else {
                range = range + changeAmount;
            }
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_AQUA + new TextComponentTranslation("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    public boolean anchorBlocks(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        int range = getToolRange(stack);
        ExchangerTool.toolModes mode = getToolMode(stack);
        ArrayList<BlockPos> currentCoords = getAnchor(stack);

        if (currentCoords.size() == 0) { //If we don't have an anchor, find the block we're supposed to anchor to
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            IBlockState setBlock = getToolBlock(stack);
            if (startBlock == null || world.getBlockState(startBlock) == Blocks.AIR.getDefaultState()) { //If we are looking at air, exit
                return false;
            }
            ArrayList<BlockPos> coords = ExchangingModes.getBuildOrders(world, player, startBlock, sideHit, range, mode, setBlock); //Build the positions list based on tool mode and range
            setAnchor(stack, coords);//Set the anchor NBT
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else { //If theres already an anchor, remove it.
            setAnchor(stack, new ArrayList<BlockPos>());
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
        return true;
    }

    public boolean exchange(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        int range = getToolRange(stack);
        ExchangerTool.toolModes mode = getToolMode(stack);
        ArrayList<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) { //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            IBlockState setBlock = getToolBlock(stack);
            coords = ExchangingModes.getBuildOrders(world, player, startBlock, sideHit, range, mode, setBlock);
        } else { //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof ExchangerTool)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof ExchangerTool)) {
                return false;
            }
        }
        IBlockState blockState = getToolBlock(heldItem);

        if (blockState != Blocks.AIR.getDefaultState()) {  //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            IBlockState state = Blocks.AIR.getDefaultState(); //Initialize a new State Variable for use in the fake world
            fakeWorld.setWorldAndState(player.world, blockState, coordinates); // Initialize the fake world's blocks
            for (BlockPos coordinate : coords) {
                if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try {
                        state = blockState.getActualState(fakeWorld, coordinate);  //Get the state of the block in the fake world (This lets fences be connected, etc)
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
        ItemStack itemStack;
        //ItemStack itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        if (setBlock.getBlock().canSilkHarvest(world, pos, setBlock, player)) {
            itemStack = InventoryManipulation.getSilkTouchDrop(setBlock);
        } else {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }
        ItemStack tool = player.getHeldItemMainhand();
        if (!(tool.getItem() instanceof ExchangerTool)) {
            tool = player.getHeldItemOffhand();
            if (!(tool.getItem() instanceof ExchangerTool)) {
                return false;
            }
        }
        NonNullList<ItemStack> drops = NonNullList.create();
        setBlock.getBlock().getDrops(drops, world, pos, setBlock, 0);
        int neededItems = 0;
        for (ItemStack drop : drops) {
            if (drop.getItem().equals(itemStack.getItem())) {
                neededItems++;
            }
        }
        if (neededItems == 0) {
            neededItems = 1;
        }
        if (InventoryManipulation.countItem(itemStack, player) < neededItems) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }
        //if (!player.canPlayerEdit(pos,EnumFacing.UP,tool)) {return false;}
        if (!world.isBlockModifiable(player, pos)) {
            return false;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()) {
            return false;
        }
        currentBlock.getBlock().harvestBlock(world, player, pos, currentBlock, world.getTileEntity(pos), tool);

        InventoryManipulation.useItem(itemStack, player, neededItems);
        world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 3));
        return true;
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

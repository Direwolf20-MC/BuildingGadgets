package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.ModItems;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.tools.BuildingModes;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.UndoState;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.direwolf20.buildinggadgets.tools.GadgetUtils.*;


public class BuildingTool extends GenericGadget {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();


    public enum toolModes {
        BuildToMe, VerticalColumn, HorizontalColumn, VerticalWall, HorizontalWall, Stairs, Checkerboard, Brush;
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
        if (!Config.poweredByFE) {
            setMaxDamage(Config.durabilityBuilder);
        }
    }

    public static void setToolMode(ItemStack stack, toolModes mode) {
        //Store the tool's mode in NBT as a string
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setString("mode", mode.name());
        stack.setTagCompound(tagCompound);
    }

    public static toolModes getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        toolModes mode = toolModes.BuildToMe;
        if (tagCompound == null) {
            setToolMode(stack, mode);
            return mode;
        }
        try {
            mode = toolModes.valueOf(tagCompound.getString("mode"));
        } catch (Exception e) {
            setToolMode(stack, mode);
        }
        return mode;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag b) {
        //Add tool information to the tooltip
        super.addInformation(stack, player, list, b);
        list.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.gadget.block") + ": " + getToolBlock(stack).getBlock().getLocalizedName());
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));
        if (getToolMode(stack) != toolModes.BuildToMe) {
            list.add(TextFormatting.RED + I18n.format("tooltip.gadget.range") + ": " + getToolRange(stack));
        }
        if (Config.poweredByFE) {
            IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
            list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        //On item use, if sneaking, select the block clicked on, else build -- This is called when a block in clicked on
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
        //On item use, if sneaking, select the block clicked on, else build -- This is called when you right click a tool NOT on a block.
        ItemStack itemstack = player.getHeldItem(hand);
        /*NBTTagCompound tagCompound = itemstack.getTagCompound();
        ByteBuf buf = Unpooled.buffer(16);
        ByteBufUtils.writeTag(buf,tagCompound);
        System.out.println(buf.readableBytes());*/
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

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {
        //Called when the mode toggle hotkey is pressed
        toolModes mode = getToolMode(heldItem);
        mode = mode.next();
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode.name()), true);
    }

    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        toolModes mode = toolModes.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode.name()), true);
    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        //Called when the range change hotkey is pressed
        int range = getToolRange(heldItem);
        if (player.isSneaking()) {
            range = (range == 1) ? Config.maxRange : range - 1;
        } else {
            range = (range >= Config.maxRange) ? 1 : range + 1;
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_AQUA + new TextComponentTranslation("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    public boolean build(EntityPlayer player, ItemStack stack) {
        //Build the blocks as shown in the visual render
        World world = player.world;
        ArrayList<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) {  //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            coords = BuildingModes.getBuildOrders(world, player, startBlock, sideHit, stack);
        } else { //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        ArrayList<BlockPos> undoCoords = new ArrayList<BlockPos>();
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof BuildingTool)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof BuildingTool)) {
                return false;
            }
        }
        IBlockState blockState = getToolBlock(heldItem);

        if (blockState != Blocks.AIR.getDefaultState()) { //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            IBlockState state = Blocks.AIR.getDefaultState(); //Initialize a new State Variable for use in the fake world
            fakeWorld.setWorldAndState(player.world, blockState, coordinates); // Initialize the fake world's blocks
            for (BlockPos coordinate : coords) {
                if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try { //Get the state of the block in the fake world (This lets fences be connected, etc)
                        state = blockState.getActualState(fakeWorld, coordinate);
                    } catch (Exception var8) {
                    }
                }
                //Get the extended block state in the fake world
                //Disabled to fix Chisel
                //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                if (placeBlock(world, player, coordinate, state)) {
                    undoCoords.add(coordinate);//If we successfully place the block, add the location to the undo list.
                }
            }
            if (undoCoords.size() > 0) { //If the undo list has any data in it, add it to NBT on the tool.
                UndoState undoState = new UndoState(player.dimension, undoCoords);
                pushUndoList(heldItem, undoState);
            }
        }
        BuildingModes.sortByDistance(coords, player);
        return true;
    }

    public static boolean undoBuild(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof BuildingTool)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof BuildingTool)) {
                return false;
            }
        }
        UndoState undoState = popUndoList(heldItem); //Get the undo list off the tool, exit if empty
        if (undoState == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.nothingtoundo").getUnformattedComponentText()), true);
            return false;
        }
        World world = player.world;
        if (!world.isRemote) {
            IBlockState currentBlock = Blocks.AIR.getDefaultState();
            ArrayList<BlockPos> undoCoords = undoState.coordinates; //Get the Coords to undo
            int dimension = undoState.dimension; //Get the Dimension to undo
            ArrayList<BlockPos> failedRemovals = new ArrayList<BlockPos>(); //Build a list of removals that fail
            ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
            silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
            for (BlockPos coord : undoCoords) {
                currentBlock = world.getBlockState(coord);
                ItemStack itemStack = currentBlock.getBlock().getPickBlock(currentBlock, null, world, coord, player);
                double distance = coord.getDistance(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
                boolean sameDim = (player.dimension == dimension);
                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, coord, currentBlock, player);
                boolean cancelled = MinecraftForge.EVENT_BUS.post(e);
                if (distance < 64 && sameDim && currentBlock != ModBlocks.effectBlock.getDefaultState() && !cancelled) { //Don't allow us to undo a block while its still being placed or too far away
                    if (currentBlock != Blocks.AIR.getDefaultState()) {
                        currentBlock.getBlock().harvestBlock(world, player, coord, currentBlock, world.getTileEntity(coord), silkTool);
                        world.spawnEntity(new BlockBuildEntity(world, coord, player, currentBlock, 2, getToolActualBlock(heldItem), false));
                    }
                } else { //If you're in the wrong dimension or too far away, fail the undo.
                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.undofailed").getUnformattedComponentText()), true);
                    failedRemovals.add(coord);
                }
            }
            if (failedRemovals.size() != 0) { //Add any failed undo blocks to the undo stack.
                UndoState failedState = new UndoState(player.dimension, failedRemovals);
                pushUndoList(heldItem, failedState);
            }
        }
        return true;
    }

    public static boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        ItemStack heldItem = player.getHeldItemMainhand();
        boolean useConstructionPaste = false;
        if (!(heldItem.getItem() instanceof BuildingTool)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof BuildingTool)) {
                return false;
            }
        }
        ItemStack itemStack;
        if (setBlock.getBlock().canSilkHarvest(world, pos, setBlock, player)) {
            itemStack = InventoryManipulation.getSilkTouchDrop(setBlock);
        } else {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }
        if (player.isSpectator()) {
            return false;
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
        if (!world.isBlockModifiable(player, pos)) {
            return false;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()) {
            return false;
        }
        ItemStack constructionPaste = new ItemStack(ModItems.constructionPaste);
        if (InventoryManipulation.countItem(itemStack, player) < neededItems) {
            //if (InventoryManipulation.countItem(constructionStack, player) == 0) {
            if (InventoryManipulation.countPaste(player) < neededItems) {
                return false;
            } else {
                itemStack = constructionPaste.copy();
                useConstructionPaste = true;
            }
        }
        if (Config.poweredByFE) {
            if (!useEnergy(heldItem, Config.energyCostBuilder, player)) {
                return false;
            }
        } else {
            if (heldItem.getItemDamage() >= heldItem.getMaxDamage()) {
                return false;
            } else {
                heldItem.damageItem(1, player);
            }
        }
        //ItemStack constructionStack = InventoryManipulation.getSilkTouchDrop(ModBlocks.constructionBlock.getDefaultState());
        boolean useItemSuccess;
        if (useConstructionPaste) {
            useItemSuccess = InventoryManipulation.usePaste(player, neededItems);
        } else {
            useItemSuccess = InventoryManipulation.useItem(itemStack, player, neededItems);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 1, getToolActualBlock(heldItem), useConstructionPaste));
            return true;
        }
        return false;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }

}

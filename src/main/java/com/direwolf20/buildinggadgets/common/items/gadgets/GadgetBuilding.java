package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.items.FakeBuilderWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.tools.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.*;

public class GadgetBuilding extends GadgetGeneric {

    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public GadgetBuilding() {
        super("buildingtool");
        setMaxDamage(SyncedConfig.durabilityBuilder);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return SyncedConfig.poweredByFE ? 0 : SyncedConfig.durabilityBuilder;
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return SyncedConfig.energyCostBuilder;
    }

    @Override
    public int getDamageCost(ItemStack tool) {
        return SyncedConfig.damageCostBuilder;
    }

    private static void setToolMode(ItemStack tool, BuildingModes mode) {
        //Store the tool's mode in NBT as a string
        NBTTagCompound tagCompound = NBTTool.getOrNewTag(tool);
        tagCompound.setString("mode", mode.getRegistryName());
    }

    public static BuildingModes getToolMode(ItemStack tool) {
        NBTTagCompound tagCompound = NBTTool.getOrNewTag(tool);
        return BuildingModes.byName(tagCompound.getString("mode"));
    }

    public static boolean shouldPlaceAtop(ItemStack stack) {
        return !NBTTool.getOrNewTag(stack).getBoolean("start_inside");
    }

    public static void togglePlaceAtop(EntityPlayer player, ItemStack stack) {
        NBTTool.getOrNewTag(stack).setBoolean("start_inside", shouldPlaceAtop(stack));
        String prefix = "message.gadget.building.placement";
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation(prefix, new TextComponentTranslation(prefix + (shouldPlaceAtop(stack) ? ".atop" : ".inside"))).getUnformattedComponentText()), true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        //Add tool information to the tooltip
        super.addInformation(stack, world, list, b);
        list.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.gadget.block") + ": " + getToolBlock(stack).getBlock().getLocalizedName());
        BuildingModes mode = getToolMode(stack);
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + (mode == BuildingModes.Surface && getConnectedArea(stack) ? I18n.format("tooltip.gadget.connected") + " " : "") + mode);
        if (getToolMode(stack) != BuildingModes.TargetedAxisChasing)
            list.add(TextFormatting.LIGHT_PURPLE + I18n.format("tooltip.gadget.range") + ": " + getToolRange(stack));

        if (getToolMode(stack) == BuildingModes.Surface)
            list.add(TextFormatting.GOLD + I18n.format("tooltip.gadget.fuzzy") + ": " + getFuzzy(stack));

        addInformationRayTraceFluid(list, stack);
        list.add(TextFormatting.YELLOW + I18n.format("tooltip.gadget.building.place_atop") + ": " + shouldPlaceAtop(stack));
        addEnergyInformation(list, stack);
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
        } else if (!player.isSneaking()) {
            ToolRenders.updateInventoryCache();
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {//TODO unused
        setMode(player, heldItem, getToolMode(heldItem).next().ordinal());
    }

    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        BuildingModes mode = BuildingModes.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode), true);
    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        //Called when the range change hotkey is pressed
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) != BuildingModes.Surface || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking())
            range = (range == 1) ? SyncedConfig.maxRange : range - changeAmount;
        else
            range = (range >= SyncedConfig.maxRange) ? 1 : range + changeAmount;

        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_AQUA + new TextComponentTranslation("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    private boolean build(EntityPlayer player, ItemStack stack) {
        //Build the blocks as shown in the visual render
        World world = player.world;
        List<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) {  //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorTools.getLookingAt(player, stack);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            coords = BuildingModes.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock);
        } else { //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        List<BlockPos> undoCoords = new ArrayList<BlockPos>();
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

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
            GadgetUtils.clearCachedRemoteInventory();
            if (undoCoords.size() > 0) { //If the undo list has any data in it, add it to NBT on the tool.
                UndoState undoState = new UndoState(player.dimension, undoCoords);
                pushUndoList(heldItem, undoState);
            }
        }

        Sorter.Blocks.byDistance(coords, player);
        return true;
    }

    public boolean undoBuild(EntityPlayer player) {
        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        UndoState undoState = popUndoList(heldItem); //Get the undo list off the tool, exit if empty
        if (undoState == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.nothingtoundo").getUnformattedComponentText()), true);
            return false;
        }
        World world = player.world;
        if (!world.isRemote) {
            IBlockState currentBlock = Blocks.AIR.getDefaultState();
            List<BlockPos> undoCoords = undoState.coordinates; //Get the Coords to undo
            int dimension = undoState.dimension; //Get the Dimension to undo
            List<BlockPos> failedRemovals = new ArrayList<BlockPos>(); //Build a list of removals that fail
            ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
            silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
            for (BlockPos coord : undoCoords) {
                currentBlock = world.getBlockState(coord);
//                ItemStack itemStack = currentBlock.getBlock().getPickBlock(currentBlock, null, world, coord, player);
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
            GadgetUtils.clearCachedRemoteInventory();
            if (failedRemovals.size() != 0) { //Add any failed undo blocks to the undo stack.
                UndoState failedState = new UndoState(player.dimension, failedRemovals);
                pushUndoList(heldItem, failedState);
            }
        }
        return true;
    }

    private boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        if (!player.isAllowEdit())
            return false;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        boolean useConstructionPaste = false;

        ItemStack itemStack;
        if (setBlock.getBlock().canSilkHarvest(world, pos, setBlock, player)) {
            itemStack = InventoryManipulation.getSilkTouchDrop(setBlock);
        } else {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }
        if (itemStack.getItem().equals(Items.AIR)) {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
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
        if (InventoryManipulation.countItem(itemStack, player, world) < neededItems) {
            //if (InventoryManipulation.countItem(constructionStack, player) == 0) {
            if (InventoryManipulation.countPaste(player) < neededItems) {
                return false;
            }
            itemStack = constructionPaste.copy();
            useConstructionPaste = true;
        }

        if (!this.canUse(heldItem, player))
            return false;

        this.applyDamage(heldItem, player);

        //ItemStack constructionStack = InventoryManipulation.getSilkTouchDrop(ModBlocks.constructionBlock.getDefaultState());
        boolean useItemSuccess;
        if (useConstructionPaste) {
            useItemSuccess = InventoryManipulation.usePaste(player, 1);
        } else {
            useItemSuccess = InventoryManipulation.useItem(itemStack, player, neededItems, world);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 1, getToolActualBlock(heldItem), useConstructionPaste));
            return true;
        }
        return false;
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack stack = GadgetGeneric.getGadget(player);
        if (!(stack.getItem() instanceof GadgetBuilding))
            return ItemStack.EMPTY;

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }

}

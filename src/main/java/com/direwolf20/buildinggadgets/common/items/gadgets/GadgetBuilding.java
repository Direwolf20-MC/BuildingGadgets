package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.IAtopPlacingGadget;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.ToolRenders;
import com.direwolf20.buildinggadgets.common.util.tools.UndoState;
import com.direwolf20.buildinggadgets.common.util.tools.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.text.ITextComponent;
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

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetBuilding extends GadgetPlacing implements IAtopPlacingGadget {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public GadgetBuilding(Properties builder) {
        super(builder);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_BUILDING.maxEnergy.get();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.GADGETS.poweredByFE.get() ? 0 : Config.GADGETS.GADGET_BUILDING.durability.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_BUILDING.energyCost.get();
    }

    @Override
    public int getDamageCost(ItemStack tool) {
        return Config.GADGETS.GADGET_BUILDING.durabilityCost.get();
    }

    @Override
    public boolean placeAtop(ItemStack stack) {
        return shouldPlaceAtop(stack);
    }

    private static void setToolMode(ItemStack tool, BuildingMode mode) {
        //Store the tool's mode in NBT as a string
        NBTTagCompound tagCompound = NBTHelper.getOrNewTag(tool);
        tagCompound.setString("mode", mode.getRegistryName());
    }

    public static BuildingMode getToolMode(ItemStack tool) {
        NBTTagCompound tagCompound = NBTHelper.getOrNewTag(tool);
        return BuildingMode.byName(tagCompound.getString("mode"));
    }

    public static boolean shouldPlaceAtop(ItemStack stack) {
        return !NBTHelper.getOrNewTag(stack).getBoolean(NBTKeys.GADGET_PLACE_INSIDE);
    }

    public static void togglePlaceAtop(EntityPlayer player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).setBoolean(NBTKeys.GADGET_PLACE_INSIDE, shouldPlaceAtop(stack));
        String prefix = "message.gadget.building.placement";
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation(prefix, new TextComponentTranslation(prefix + (shouldPlaceAtop(stack) ? ".atop" : ".inside"))).getUnformattedComponentText()), true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(TooltipTranslation.GADGET_BLOCK
                            .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack)))
                            .setStyle(Styles.DK_GREEN));
        BuildingMode mode = getToolMode(stack);
        tooltip.add(TooltipTranslation.GADGET_MODE
                            .componentTranslation((mode == BuildingMode.SURFACE && getConnectedArea(stack) ? TooltipTranslation.GADGET_CONNECTED
                                    .format(mode) : mode))
                            .setStyle(Styles.AQUA));
        if (getToolMode(stack) != BuildingMode.TARGETED_AXIS_CHASING)
            tooltip.add(TooltipTranslation.GADGET_RANGE
                        .componentTranslation(getToolRange(stack))
                         .setStyle(Styles.LT_PURPLE));

        if (getToolMode(stack) == BuildingMode.SURFACE)
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                                .componentTranslation(String.valueOf(getFuzzy(stack)))
                                .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP
                            .componentTranslation(String.valueOf(shouldPlaceAtop(stack)))
                            .setStyle(Styles.YELLOW));
        addEnergyInformation(tooltip, stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        //On item use, if sneaking, select the block clicked on, else build -- This is called when you right click a tool NOT on a block.
        ItemStack itemstack = player.getHeldItem(hand);
        /*NBTTagCompound tagCompound = itemstack.getTag();
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
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {//TODO unused
        setMode(player, heldItem, getToolMode(heldItem).next().ordinal());
    }

    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        BuildingMode mode = BuildingMode.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode), true);
    }

    public static void rangeChange(EntityPlayer player, ItemStack heldItem) {
        //Called when the range change hotkey is pressed
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) != BuildingMode.SURFACE || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking())
            range = (range == 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        else
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;

        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_AQUA + new TextComponentTranslation("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    private boolean build(EntityPlayer player, ItemStack stack) {
        //Build the blocks as shown in the visual render
        World world = player.world;
        List<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) {  //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            coords = BuildingMode.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock);
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
// @todo: reimplement @since 1.13.x
                        state = blockState.getExtendedState(fakeWorld, coordinate);
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

        SortingHelper.Blocks.byDistance(coords, player);
        return true;
    }

    public static boolean undoBuild(EntityPlayer player) {
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

            List<BlockPos> failedRemovals = new ArrayList<BlockPos>(); //Build a list of removals that fail
            ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
            silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
            boolean sameDim = player.dimension == undoState.dimension;
            for (BlockPos coord : undoCoords) {
                currentBlock = world.getBlockState(coord);

                double distance = coord.getDistance(player.getPosition());

                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, coord, currentBlock, player);
                boolean cancelled = MinecraftForge.EVENT_BUS.post(e);

                if (distance < 64 && sameDim && currentBlock != BGBlocks.effectBlock.getDefaultState() && !cancelled) { //Don't allow us to undo a block while its still being placed or too far away
                    if (currentBlock != Blocks.AIR.getDefaultState()) {
                        currentBlock.getBlock().harvestBlock(world, player, coord, currentBlock, world.getTileEntity(coord), silkTool);
                        world.spawnEntity(new BlockBuildEntity(world, coord, player, currentBlock, BlockBuildEntity.Mode.REMOVE, false));
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

    private boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        if (!player.isAllowEdit())
            return false;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        boolean useConstructionPaste = false;

        ItemStack itemStack;
        if (setBlock.getBlock().canSilkHarvest(setBlock, world, pos, player)) {
            itemStack = InventoryHelper.getSilkTouchDrop(setBlock);
        } else {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }
        if (itemStack.getItem().equals(Items.AIR)) {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        setBlock.getBlock().getDrops(setBlock, drops, world, pos, 0);
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
        if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, EnumFacing.UP)) {
            return false;
        }
        ItemStack constructionPaste = new ItemStack(BGItems.constructionPaste);
        if (InventoryHelper.countItem(itemStack, player, world) < neededItems) {
            //if (InventoryHelper.countItem(constructionStack, player) == 0) {
            if (InventoryHelper.countPaste(player) < neededItems) {
                return false;
            }
            itemStack = constructionPaste.copy();
            useConstructionPaste = true;
        }

        if (!this.canUse(heldItem, player))
            return false;

        this.applyDamage(heldItem, player);

        //ItemStack constructionStack = InventoryHelper.getSilkTouchDrop(ModBlocks.constructionBlock.getDefaultState());
        boolean useItemSuccess;
        if (useConstructionPaste) {
            useItemSuccess = InventoryHelper.usePaste(player, 1);
        } else {
            useItemSuccess = InventoryHelper.useItem(itemStack, player, neededItems, world);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, BlockBuildEntity.Mode.PLACE, useConstructionPaste));
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
    public int getUseDuration(ItemStack stack) {
        return 20;
    }
}

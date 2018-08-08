package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.ModItems;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.tools.ExchangingModes;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.direwolf20.buildinggadgets.tools.GadgetUtils.*;

public class ExchangerTool extends GenericGadget {
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
        if (!Config.poweredByFE) {
            setMaxDamage(Config.durabilityExchanger);
        }
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
        } else {
            return super.isBookEnchantable(stack, book);
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public static void setFuzzy(ItemStack stack, boolean fuzzy) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setBoolean("fuzzy", fuzzy);
    }

    public static boolean getFuzzy(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        return tagCompound.getBoolean("fuzzy");
    }

    public static void setToolMode(ItemStack stack, ExchangerTool.toolModes mode) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setString("mode", mode.name());
        stack.setTagCompound(tagCompound);
    }

    public static ExchangerTool.toolModes getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        toolModes mode = toolModes.Wall;
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
        super.addInformation(stack, player, list, b);
        list.add(TextFormatting.DARK_GREEN + I18n.format("tooltip.gadget.block") + ": " + getToolBlock(stack).getBlock().getLocalizedName());
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));
        list.add(TextFormatting.RED + I18n.format("tooltip.gadget.range") + ": " + getToolRange(stack));
        if (Config.poweredByFE) {
            IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
            list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
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

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {
        ExchangerTool.toolModes mode = getToolMode(heldItem);
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

    public void toggleFuzzy(EntityPlayer player, ItemStack heldItem) {
        setFuzzy(heldItem, !(getFuzzy(heldItem)));
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.fuzzymode").getUnformattedComponentText() + ": " + getFuzzy(heldItem)), true);
    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) == toolModes.Checkerboard || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking()) {
            range = (range <= 1) ? Config.maxRange : range - changeAmount;
        } else {
            range = (range >= Config.maxRange) ? 1 : range + changeAmount;
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_AQUA + new TextComponentTranslation("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    public boolean exchange(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        ArrayList<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) { //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
            IBlockState setBlock = getToolBlock(stack);
            coords = ExchangingModes.getBuildOrders(world, player, startBlock, sideHit, stack);
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
        boolean useConstructionPaste = false;
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
            ItemStack constructionPaste = new ItemStack(ModItems.constructionPaste);
            if (InventoryManipulation.countPaste(player) < neededItems) {
                return false;
            } else {
                itemStack = constructionPaste.copy();
                useConstructionPaste = true;
            }
        }
        if (player.isSpectator()) {
            return false;
        }
        if (!world.isBlockModifiable(player, pos)) {
            return false;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()) {
            return false;
        }
        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, pos, currentBlock, player);
        if (MinecraftForge.EVENT_BUS.post(e)) {
            return false;
        }
        if (Config.poweredByFE) {
            if (!useEnergy(tool, Config.energyCostBuilder, player)) {
                return false;
            }
        } else {
            if (tool.getItemDamage() >= tool.getMaxDamage()) {
                return false;
            } else {
                tool.damageItem(2, player);
            }
        }
        currentBlock.getBlock().harvestBlock(world, player, pos, currentBlock, world.getTileEntity(pos), tool);
        boolean useItemSuccess;
        if (useConstructionPaste) {
            useItemSuccess = InventoryManipulation.usePaste(player, neededItems);
        } else {
            useItemSuccess = InventoryManipulation.useItem(itemStack, player, neededItems);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 3, getToolActualBlock(tool), useConstructionPaste));
            return true;
        }
        return false;
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

package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil.EnergyUtil;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.tools.ToolRenders;
import com.direwolf20.buildinggadgets.common.util.tools.modes.ExchangingMode;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

public class GadgetExchanger extends GadgetSwapping {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public GadgetExchanger(Properties builder) {
        super(builder);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_EXCHANGER.maxEnergy.get();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.GADGETS.poweredByFE.get() ? 0 : Config.GADGETS.GADGET_EXCHANGER.durability.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_EXCHANGER.energyCost.get();
    }

    @Override
    public int getDamageCost(ItemStack tool) {
        return Config.GADGETS.GADGET_EXCHANGER.durabilityCost.get();
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
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    private static void setToolMode(ItemStack tool, ExchangingMode mode) {
        //Store the tool's mode in NBT as a string
        NBTTagCompound tagCompound = NBTHelper.getOrNewTag(tool);
        tagCompound.setString("mode", mode.getRegistryName());
    }

    public static ExchangingMode getToolMode(ItemStack tool) {
        NBTTagCompound tagCompound = NBTHelper.getOrNewTag(tool);
        return ExchangingMode.byName(tagCompound.getString("mode"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(TooltipTranslation.GADGET_BLOCK
                            .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack)))
                            .setStyle(Styles.DK_GREEN));
        ExchangingMode mode = getToolMode(stack);
        tooltip.add(TooltipTranslation.GADGET_MODE
                            .componentTranslation((mode == ExchangingMode.SURFACE && getConnectedArea(stack) ? TooltipTranslation.GADGET_CONNECTED
                                    .format(mode) : mode))
                            .setStyle(Styles.AQUA));
        tooltip.add(TooltipTranslation.GADGET_RANGE
                            .componentTranslation(getToolRange(stack))
                            .setStyle(Styles.LT_PURPLE));
        tooltip.add(TooltipTranslation.GADGET_FUZZY
                            .componentTranslation(String.valueOf(getFuzzy(stack)))
                            .setStyle(Styles.GOLD));
        addInformationRayTraceFluid(tooltip, stack);
        addEnergyInformation(tooltip, stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                //TODO Remove debug code
                EnergyUtil.getCap(itemstack).ifPresent(energy -> energy.receiveEnergy(105000, false));
                selectBlock(itemstack, player);
            } else {
                exchange(player, itemstack);
            }
        } else if (!player.isSneaking()) {
            ToolRenders.updateInventoryCache();
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {//TODO unused
        setToolMode(heldItem, getToolMode(heldItem).next());
    }

    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        ExchangingMode mode = ExchangingMode.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode), true);
    }

    public static void rangeChange(EntityPlayer player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) == ExchangingMode.GRID || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking()) {
            range = (range <= 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        } else {
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_AQUA + new TextComponentTranslation("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    private boolean exchange(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        List<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) { //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if (lookingAt == null) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = lookingAt.getBlockPos();
            EnumFacing sideHit = lookingAt.sideHit;
//            IBlockState setBlock = getToolBlock(stack);
            coords = ExchangingMode.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock);
        } else { //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        IBlockState blockState = getToolBlock(heldItem);

        if (blockState != Blocks.AIR.getDefaultState()) {  //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            IBlockState state = Blocks.AIR.getDefaultState(); //Initialize a new State Variable for use in the fake world
            fakeWorld.setWorldAndState(player.world, blockState, coordinates); // Initialize the fake world's blocks
            for (BlockPos coordinate : coords) {
                if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try {
                        state = blockState.getExtendedState(world/*fakework*/, coordinate);  //Get the state of the block in the fake world (This lets fences be connected, etc)
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

    private boolean exchangeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        IBlockState currentBlock = world.getBlockState(pos);
        ItemStack itemStack;
        boolean useConstructionPaste = false;
        //ItemStack itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        if (setBlock.getBlock().canSilkHarvest(setBlock, world, pos, player)) {
            itemStack = InventoryHelper.getSilkTouchDrop(setBlock);
        } else {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }
        if (itemStack.getItem().equals(Items.AIR)) {
            itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);
        }

        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

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
        if (InventoryHelper.countItem(itemStack, player, world) < neededItems) {
            ItemStack constructionPaste = new ItemStack(BGItems.constructionPaste);
            if (InventoryHelper.countPaste(player) < neededItems) {
                return false;
            }
            itemStack = constructionPaste.copy();
            useConstructionPaste = true;
        }
        if (!player.isAllowEdit()) {
            return false;
        }
        if (!world.isBlockModifiable(player, pos)) {
            return false;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (!ForgeEventFactory.onBlockPlace(player, blockSnapshot, EnumFacing.UP)) {
            return false;
        }
        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, pos, currentBlock, player);
        if (MinecraftForge.EVENT_BUS.post(e)) {
            return false;
        }

        if (!this.canUse(tool, player))
            return false;

        this.applyDamage(tool, player);

        currentBlock.getBlock().harvestBlock(world, player, pos, currentBlock, world.getTileEntity(pos), tool);
        boolean useItemSuccess;
        if (useConstructionPaste) {
            useItemSuccess = InventoryHelper.usePaste(player, 1);
        } else {
            useItemSuccess = InventoryHelper.useItem(itemStack, player, neededItems, world);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, BlockBuildEntity.MODE_REPLACE, useConstructionPaste));
            return true;
        }
        return false;
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack stack = GadgetGeneric.getGadget(player);
        if (!(stack.getItem() instanceof GadgetExchanger))
            return ItemStack.EMPTY;

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

}

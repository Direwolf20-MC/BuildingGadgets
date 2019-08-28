package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.modes.IAtopPlacingGadget;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BuildingRender;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.save.SaveManager;
import com.direwolf20.buildinggadgets.common.save.UndoWorldSave;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UndoState;
import com.direwolf20.buildinggadgets.common.util.tools.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetBuilding extends AbstractGadget implements IAtopPlacingGadget {

    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public GadgetBuilding(Properties builder) {
        super(builder);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_BUILDING.maxEnergy.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_BUILDING.energyCost.get();
    }

    @Override
    protected Supplier<BaseRenderer> createRenderFactory() {
        return BuildingRender::new;
    }

    @Override
    protected UndoWorldSave getUndoSave() {
        return SaveManager.INSTANCE.getBuildingUndo();
    }

    @Override
    public boolean placeAtop(ItemStack stack) {
        return shouldPlaceAtop(stack);
    }

    private static void setToolMode(ItemStack tool, BuildingMode mode) {
        //Store the tool's mode in NBT as a string
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(tool);
        tagCompound.putString("mode", mode.getRegistryName());
    }

    public static BuildingMode getToolMode(ItemStack tool) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(tool);
        return BuildingMode.byName(tagCompound.getString("mode"));
    }

    public static boolean shouldPlaceAtop(ItemStack stack) {
        return !NBTHelper.getOrNewTag(stack).getBoolean(NBTKeys.GADGET_PLACE_INSIDE);
    }

    public static void togglePlaceAtop(PlayerEntity player, ItemStack stack) {
        NBTHelper.getOrNewTag(stack).putBoolean(NBTKeys.GADGET_PLACE_INSIDE, shouldPlaceAtop(stack));
        String prefix = "message.gadget.building.placement";
        player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent(prefix, new TranslationTextComponent(prefix + (shouldPlaceAtop(stack) ? ".atop" : ".inside"))).getUnformattedComponentText()), true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(TooltipTranslation.GADGET_BLOCK
                .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack).getState()))
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
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        //On item use, if sneaking, select the block clicked on, else build -- This is called when you right click a tool NOT on a block.
        ItemStack itemstack = player.getHeldItem(hand);
        //remove once we go live - debug code to add free energy to tool
        //IEnergyStorage energy = CapabilityUtil.EnergyUtil.getCap(itemstack).orElseThrow(CapabilityNotPresentException::new);
        //energy.receiveEnergy(100000, false);
        /*CompoundNBT tagCompound = itemstack.getTag();
        ByteBuf buf = Unpooled.buffer(16);
        ByteBufUtils.writeTag(buf,tagCompound);
        System.out.println(buf.readableBytes());*/
        // System.out.println("jo");
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(itemstack, player);
            } else if (player instanceof ServerPlayerEntity) {
                build((ServerPlayerEntity) player, itemstack);
            }
            // System.out.println("hello");
        } else {
            // System.out.println("ss");

            if (!player.isSneaking()) {
                BaseRenderer.updateInventoryCache();
            } else {
                if (Screen.hasControlDown()) {
                    System.out.println("CUnt");
                    PacketHandler.sendToServer(new PacketBindTool());
                }
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

    public void setMode(ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        BuildingMode mode = BuildingMode.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    public static void rangeChange(PlayerEntity player, ItemStack heldItem) {
        //Called when the range change hotkey is pressed
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) != BuildingMode.SURFACE || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking())
            range = (range == 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        else
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;

        setToolRange(heldItem, range);
        player.sendStatusMessage(new StringTextComponent(TextFormatting.DARK_AQUA + new TranslationTextComponent("message.gadget.toolrange").getUnformattedComponentText() + ": " + range), true);
    }

    private boolean build(ServerPlayerEntity player, ItemStack stack) {
        //Build the blocks as shown in the visual render
        World world = player.world;
        List<BlockPos> coords = getAnchor(stack);

        if (coords.size() == 0) {  //If we don't have an anchor, build in the current spot
            RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if (lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState())) { //If we aren't looking at anything, exit
                return false;
            }
            BlockPos startBlock = ((BlockRayTraceResult) lookingAt).getPos();
            Direction sideHit = ((BlockRayTraceResult) lookingAt).getFace();
            coords = BuildingMode.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock);
        } else { //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        List<BlockPos> undoCoords = new ArrayList<BlockPos>();

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        BlockData blockData = getToolBlock(heldItem);

        if (blockData.getState() != Blocks.AIR.getDefaultState()) { //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            //TODO replace with a better TileEntity supporting Fake IWorld
            fakeWorld.setWorldAndState(player.world, blockData.getState(), coords); // Initialize the fake world's blocks
            for (BlockPos coordinate : coords) {
                //Get the extended block state in the fake world
                //Disabled to fix Chisel
                //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                if (placeBlock(world, player, coordinate, blockData)) {
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

    public static boolean undoBuild(PlayerEntity player) {
        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        UndoState undoState = popUndoList(heldItem); //Get the undo list off the tool, exit if empty
        if (undoState == null) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.nothingtoundo").getUnformattedComponentText()), true);
            return false;
        }
        World world = player.world;
        if (!world.isRemote) {
            List<BlockPos> undoCoords = undoState.coordinates; //Get the Coords to undo

            List<BlockPos> failedRemovals = new ArrayList<BlockPos>(); //Build a list of removals that fail
            ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
            silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
            boolean sameDim = player.dimension == undoState.dimension;
            for (BlockPos coord : undoCoords) {
                BlockData currentBlock = TileSupport.createBlockData(world, coord);

                double distance = Math.sqrt(coord.distanceSq(player.getPosition()));

                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, coord, currentBlock.getState(), player);
                boolean cancelled = MinecraftForge.EVENT_BUS.post(e);

                if (distance < 64 && sameDim && currentBlock.getState() != BGBlocks.effectBlock.getDefaultState() && !cancelled) { //Don't allow us to undo a block while its still being placed or too far away
                    if (currentBlock.getState() != Blocks.AIR.getDefaultState()) {
                        currentBlock.getState().getBlock().harvestBlock(world, player, coord, currentBlock.getState(), world.getTileEntity(coord), silkTool);
                        EffectBlock.spawnEffectBlock(world, coord, currentBlock, EffectBlock.Mode.REMOVE, false);
                    }
                } else { //If you're in the wrong dimension or too far away, fail the undo.
                    player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.undofailed").getUnformattedComponentText()), true);
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

    private boolean placeBlock(World world, ServerPlayerEntity player, BlockPos pos, BlockData setBlock) {
        if (!player.isAllowEdit())
            return false;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        boolean useConstructionPaste = false;

        ItemStack itemStack;
        if (true/*setBlock.getBlock().canSilkHarvest(setBlock, world, pos, player)*/) {//TODO figure LootTables out
            itemStack = InventoryHelper.getSilkTouchDrop(setBlock.getState());
        } else {
            itemStack = setBlock.getState().getBlock().getPickBlock(setBlock.getState(), null, world, pos, player);
        }
        if (itemStack.getItem().equals(Items.AIR)) {
            itemStack = setBlock.getState().getBlock().getPickBlock(setBlock.getState(), null, world, pos, player);
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        //TODO figure LootTables out
        //setBlock.getBlock().getDrops(setBlock, drops, world, pos, 0);
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
        if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP)) {
            return false;
        }
        if (!setBlock.getState().hasTileEntity()) {
            ItemStack constructionPaste = new ItemStack(BGItems.constructionPaste);
            if (InventoryHelper.countItem(itemStack, player, world) < neededItems) {
                //if (InventoryHelper.countItem(constructionStack, player) == 0) {
                if (InventoryHelper.countPaste(player) < neededItems) {
                    return false;
                }
                itemStack = constructionPaste.copy();
                useConstructionPaste = true;
            }
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
            EffectBlock.spawnEffectBlock(world, pos, setBlock, EffectBlock.Mode.PLACE, useConstructionPaste);
            return true;
        }
        return false;
    }

    public static ItemStack getGadget(PlayerEntity player) {
        ItemStack stack = AbstractGadget.getGadget(player);
        if (!(stack.getItem() instanceof GadgetBuilding))
            return ItemStack.EMPTY;

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }
}

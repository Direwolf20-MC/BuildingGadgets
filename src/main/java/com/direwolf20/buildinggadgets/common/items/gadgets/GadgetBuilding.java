package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.building.modes.IAtopPlacingGadget;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BuildingRender;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import com.google.common.collect.ImmutableMultiset;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetBuilding extends ModeGadget implements IAtopPlacingGadget {

    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public GadgetBuilding(Properties builder, IntSupplier undoLengthSupplier, String undoName) {
        super(builder, undoLengthSupplier, undoName, TagReference.WHITELIST_BUILDING, TagReference.BLACKLIST_BUILDING);
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
        player.sendStatusMessage((shouldPlaceAtop(stack) ? MessageTranslation.PLACE_ATOP : MessageTranslation.PLACE_INSIDE).componentTranslation().setStyle(Styles.AQUA), true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        BuildingMode mode = getToolMode(stack);
        addEnergyInformation(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_MODE
                .componentTranslation((mode == BuildingMode.SURFACE && getConnectedArea(stack) ? TooltipTranslation.GADGET_CONNECTED
                        .format(mode) : mode))
                .setStyle(Styles.AQUA));

        tooltip.add(TooltipTranslation.GADGET_BLOCK
                .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack).getState()))
                .setStyle(Styles.DK_GREEN));

        int range = getToolRange(stack);
        if (getToolMode(stack) != BuildingMode.TARGETED_AXIS_CHASING)
            tooltip.add(TooltipTranslation.GADGET_RANGE
                    .componentTranslation(range, getRangeInBlocks(range, mode.getModeImplementation()))
                    .setStyle(Styles.LT_PURPLE));

        if (getToolMode(stack) == BuildingMode.SURFACE)
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                    .componentTranslation(String.valueOf(getFuzzy(stack)))
                    .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP
                .componentTranslation(String.valueOf(shouldPlaceAtop(stack)))
                .setStyle(Styles.YELLOW));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        //On item use, if sneaking, select the block clicked on, else build -- This is called when you right click a tool NOT on a block.
        ItemStack itemstack = player.getHeldItem(hand);

        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isShiftKeyDown()) {
                selectBlock(itemstack, player);
            } else if (player instanceof ServerPlayerEntity) {
                build((ServerPlayerEntity) player, itemstack);
            }
        } else {
            if (!player.isShiftKeyDown()) {
                BaseRenderer.updateInventoryCache();
            } else {
                if (Screen.hasControlDown())
                    PacketHandler.sendToServer(new PacketBindTool());
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
        if (player.isShiftKeyDown())
            range = (range == 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        else
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;

        setToolRange(heldItem, range);
        player.sendStatusMessage(MessageTranslation.RANGE_SET.componentTranslation(range).setStyle(Styles.AQUA), true);
    }

    private boolean build(ServerPlayerEntity player, ItemStack stack) {
        //Build the blocks as shown in the visual render
        World world = player.world;
        List<BlockPos> coords = GadgetUtils.getAnchor(stack);

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

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        BlockData blockData = getToolBlock(heldItem);
        Undo.Builder builder = Undo.builder();
        IItemIndex index = InventoryHelper.index(stack, player);
        if (blockData.getState() != Blocks.AIR.getDefaultState()) { //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            //TODO replace with a better TileEntity supporting Fake IWorld
            fakeWorld.setWorldAndState(player.world, blockData.getState(), coords); // Initialize the fake world's blocks
            for (BlockPos coordinate : coords) {
                //Get the extended block state in the fake world
                //Disabled to fix Chisel
                //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                placeBlock(world, player, index, builder, coordinate, blockData);
            }
        }
        pushUndo(stack, builder.build(world.getDimension().getType()));
        return true;
    }

    private boolean placeBlock(World world, ServerPlayerEntity player, IItemIndex index, Undo.Builder builder, BlockPos pos, BlockData setBlock) {
        if (pos.getY() > world.getMaxHeight() || pos.getY() < 0)
            return false;
        if (!player.isAllowEdit())
            return false;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        boolean useConstructionPaste = false;

        IBuildContext buildContext = SimpleBuildContext.builder()
                .usedStack(heldItem)
                .buildingPlayer(player)
                .build(world);
        MaterialList requiredItems = setBlock.getRequiredItems(buildContext, null, pos);
        MatchResult match = index.tryMatch(requiredItems);
        if (! match.isSuccess()) {
            if (setBlock.getState().hasTileEntity())
                return false;
            match = index.tryMatch(InventoryHelper.PASTE_LIST);
            if (! match.isSuccess())
                return false;
            else
                useConstructionPaste = true;
        }
        if (! world.isBlockModifiable(player, pos))
            return false;
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP)) {
            return false;
        }

        if (!this.canUse(heldItem, player))
            return false;

        this.applyDamage(heldItem, player);

        if (index.applyMatch(match)) {
            ImmutableMultiset<IUniqueObject<?>> usedItems = match.getChosenOption();
            builder.record(world, pos, setBlock, usedItems, ImmutableMultiset.of());
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
}

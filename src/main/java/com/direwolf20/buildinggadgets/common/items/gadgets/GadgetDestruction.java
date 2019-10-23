package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.placement.PlacementSequences.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.building.placement.SetBackedPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.DestructionRender;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.save.Undo;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableMultiset;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GadgetDestruction extends AbstractGadget {

    public GadgetDestruction(Properties builder, IntSupplier undoLengthSupplier, String undoName) {
        super(builder, undoLengthSupplier, undoName);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_DESTRUCTION.maxEnergy.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_DESTRUCTION.energyCost.get() * getCostMultiplier(tool);
    }

    @Override
    protected Supplier<BaseRenderer> createRenderFactory() {
        return DestructionRender::new;
    }

    private int getCostMultiplier(ItemStack tool) {
        return (int) (! getFuzzy(tool) ? Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyMultiplier.get() : 1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        addEnergyInformation(tooltip, stack);
        tooltip.add(TooltipTranslation.GADGET_DESTROYWARNING
                            .componentTranslation()
                            .setStyle(Styles.RED));
        tooltip.add(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                            .componentTranslation(String.valueOf(getOverlay(stack)))
                            .setStyle(Styles.AQUA));
        tooltip.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP
                            .componentTranslation(String.valueOf(getConnectedArea(stack)))
                            .setStyle(Styles.YELLOW));
        if (Config.isServerConfigLoaded() && Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get())
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                                .componentTranslation(String.valueOf(getFuzzy(stack)))
                                .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);
    }

    public static void setAnchor(ItemStack stack, BlockPos pos) {
        GadgetUtils.writePOSToNBT(stack, pos, NBTKeys.GADGET_ANCHOR);
    }

    public static void setAnchorSide(ItemStack stack, Direction side) {
        CompoundNBT tag = NBTHelper.getOrNewTag(stack);
        if (side == null)
            tag.remove(NBTKeys.GADGET_ANCHOR_SIDE);
        else
            tag.putString(NBTKeys.GADGET_ANCHOR_SIDE, side.getName());
    }

    public static Direction getAnchorSide(ItemStack stack) {
        CompoundNBT tag = NBTHelper.getOrNewTag(stack);
        String facing = tag.getString(NBTKeys.GADGET_ANCHOR_SIDE);
        if (facing.isEmpty())
            return null;
        return Direction.byName(facing);
    }

    public static void setToolValue(ItemStack stack, int value, String valueName) {
        NBTHelper.getOrNewTag(stack).putInt(valueName, value);
    }

    public static int getToolValue(ItemStack stack, String valueName) {
        return NBTHelper.getOrNewTag(stack).getInt(valueName);
    }

    public static boolean getOverlay(ItemStack stack) {
        CompoundNBT tag = NBTHelper.getOrNewTag(stack);
        if (tag.contains(NBTKeys.GADGET_OVERLAY))
            return tag.getBoolean(NBTKeys.GADGET_OVERLAY);

        tag.putBoolean(NBTKeys.GADGET_OVERLAY, true);
        tag.putBoolean(NBTKeys.GADGET_FUZZY, true); // We want a Destruction Gadget to start with fuzzy=true
        return true;
    }

    public static void setOverlay(ItemStack stack, boolean showOverlay) {
        NBTHelper.getOrNewTag(stack).putBoolean(NBTKeys.GADGET_OVERLAY, showOverlay);
    }

    public static void switchOverlay(PlayerEntity player, ItemStack stack) {
        boolean newOverlay = ! getOverlay(stack);
        setOverlay(stack, newOverlay);
        player.sendStatusMessage(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                .componentTranslation(newOverlay).setStyle(Styles.AQUA), true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);

        if (!world.isRemote) {
            if (! player.isSneaking()) {
                BlockPos anchorPos = getAnchor(stack);
                Direction anchorSide = getAnchorSide(stack);
                if (anchorPos != null && anchorSide != null) {
                    clearArea(world, anchorPos, anchorSide, (ServerPlayerEntity) player, stack);
                    onAnchorRemoved(stack, player);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
                if (! world.isAirBlock(lookingAt.getPos())) {
                    clearArea(world, lookingAt.getPos(), lookingAt.getFace(), (ServerPlayerEntity) player, stack);
                    onAnchorRemoved(stack, player);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                return new ActionResult<>(ActionResultType.FAIL, stack);
            }
        } else if (player.isSneaking()) {
            GuiMod.DESTRUCTION.openScreen(player);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    protected void onAnchorSet(ItemStack stack, PlayerEntity player, BlockRayTraceResult lookingAt) {
        super.onAnchorSet(stack, player, lookingAt);
        setAnchorSide(stack, lookingAt.getFace());
    }

    @Override
    protected void onAnchorRemoved(ItemStack stack, PlayerEntity player) {
        super.onAnchorRemoved(stack, player);
        setAnchorSide(stack, null);
    }

    public static IPositionPlacementSequence getClearingPositions(World world, BlockPos pos, Direction incomingSide, PlayerEntity player, ItemStack stack) {
        ItemStack tool = getGadget(player);
        GadgetDestruction item = (GadgetDestruction) tool.getItem();
        int depth = getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH);
        if (tool.isEmpty() || depth == 0 || ! player.isAllowEdit())
            return CommonUtils.emptyPositionSequence();

        Region boundary = getClearingRegion(pos, incomingSide, player, stack);
        BlockPos startPos = (item.getAnchor(stack) == null) ? pos : item.getAnchor(stack);
        boolean fuzzy = ! Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get() || AbstractGadget.getFuzzy(stack);
        BlockState stateTarget = fuzzy ? null : world.getBlockState(pos);

        if (AbstractGadget.getConnectedArea(stack))
            return ConnectedSurface.create(world, boundary, Function.identity(), startPos, null, (s, p) -> isValidBlock(world, p, player, s, fuzzy));
        else
            return new SetBackedPlacementSequence(boundary.stream()
                    .filter(p -> isValidBlock(world, p, player, stateTarget, fuzzy))
                    .collect(Collectors.toCollection(HashSet::new)), boundary);
    }

    public static List<BlockPos> getClearingPositionsForRendering(World world, BlockPos pos, Direction incomingSide, PlayerEntity player, ItemStack stack) {
        return SortingHelper.Blocks.byDistance(getClearingPositions(world, pos, incomingSide, player, stack), player);
    }

    public static boolean isValidBlock(World world, BlockPos voidPos, PlayerEntity player, @Nullable BlockState stateTarget, boolean fuzzy) {
        BlockState currentBlock = world.getBlockState(voidPos);
        return isValidBlock(world, voidPos, player, currentBlock, stateTarget, fuzzy);
    }

    public static boolean isValidBlock(World world, BlockPos voidPos, PlayerEntity player, BlockState currentBlock, @Nullable BlockState stateTarget, boolean fuzzy) {
        if (currentBlock.getBlock().isAir(currentBlock, world, voidPos) ||
                currentBlock.equals(OurBlocks.effectBlock.getDefaultState()) ||
                currentBlock.getBlockHardness(world, voidPos) < 0 ||
                (! fuzzy && currentBlock != stateTarget) ||
                ! world.isBlockModifiable(player, voidPos)) return false;

        TileEntity te = world.getTileEntity(voidPos);
        if ((te != null) && ! (te instanceof ConstructionBlockTileEntity)) return false;

        if (! world.isRemote) {
            BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, voidPos);
            if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP))
                return false;
            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, voidPos, currentBlock, player);
            return ! MinecraftForge.EVENT_BUS.post(e);
        }
        return true;
    }

    public static Region getClearingRegion(BlockPos pos, Direction side, PlayerEntity player, ItemStack stack) {
        Direction depth = side.getOpposite();
        boolean vertical = side.getAxis().isVertical();
        Direction up = vertical ? player.getHorizontalFacing() : Direction.UP;
        Direction down = up.getOpposite();
        Direction right = vertical ? up.rotateY() : side.rotateYCCW();
        Direction left = right.getOpposite();

        BlockPos first = pos.offset(left, getToolValue(stack, NBTKeys.GADGET_VALUE_LEFT))
                .offset(up, getToolValue(stack, NBTKeys.GADGET_VALUE_UP));
        BlockPos second = pos.offset(right, getToolValue(stack, NBTKeys.GADGET_VALUE_RIGHT))
                .offset(down, getToolValue(stack, NBTKeys.GADGET_VALUE_DOWN))
                .offset(depth, getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH) - 1);
        // The number are not necessarily sorted min and max, but the constructor will do it for us
        return new Region(first, second);
    }

    public void clearArea(World world, BlockPos pos, Direction side, ServerPlayerEntity player, ItemStack stack) {
        IPositionPlacementSequence positions = getClearingPositions(world, pos, side, player, stack);
        Undo.Builder builder = Undo.builder();
        for (BlockPos clearPos : positions) {
            BlockState state = world.getBlockState(clearPos);
            TileEntity te = world.getTileEntity(clearPos);
            if (te == null || state.getBlock() == OurBlocks.constructionBlock && te instanceof ConstructionBlockTileEntity) {
                destroyBlock(world, clearPos, player, builder);
            }
        }
        pushUndo(stack, builder.build(world.getDimension().getType()));
    }

    private boolean destroyBlock(World world, BlockPos voidPos, ServerPlayerEntity player, Undo.Builder builder) {
        if (world.isAirBlock(voidPos))
            return false;

        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

        if (! this.canUse(tool, player))
            return false;

        this.applyDamage(tool, player);
        builder.record(world, voidPos, BlockData.AIR, ImmutableMultiset.of(), ImmutableMultiset.of());
        EffectBlock.spawnEffectBlock(world, voidPos, TileSupport.createBlockData(world, voidPos), EffectBlock.Mode.REMOVE, false);
        return true;
    }

    public static ItemStack getGadget(PlayerEntity player) {
        ItemStack stack = AbstractGadget.getGadget(player);
        if (!(stack.getItem() instanceof GadgetDestruction))
            return ItemStack.EMPTY;

        return stack;
    }

}

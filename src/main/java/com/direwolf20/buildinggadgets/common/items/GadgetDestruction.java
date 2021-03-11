package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.client.renders.DestructionRender;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
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
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GadgetDestruction extends AbstractGadget {

    public GadgetDestruction() {
        super(OurItems.nonStackableItemProperties(),
                Config.GADGETS.GADGET_DESTRUCTION.undoSize::get,
                Reference.SaveReference.UNDO_DESTRUCTION,
                TagReference.WHITELIST_DESTRUCTION,
                TagReference.BLACKLIST_DESTRUCTION);
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
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
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

        if (Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get())
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                                .componentTranslation(String.valueOf(getFuzzy(stack)))
                                .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);
    }

    public static void setAnchor(ItemStack stack, BlockPos pos) {
        GadgetUtils.writePOSToNBT(stack, pos, NBTKeys.GADGET_ANCHOR);
    }

    public static void setAnchorSide(ItemStack stack, Direction side) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (side == null)
            tag.remove(NBTKeys.GADGET_ANCHOR_SIDE);
        else
            tag.putString(NBTKeys.GADGET_ANCHOR_SIDE, side.getName());
    }

    public static Direction getAnchorSide(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        String facing = tag.getString(NBTKeys.GADGET_ANCHOR_SIDE);
        if (facing.isEmpty())
            return null;
        return Direction.byName(facing);
    }

    public static void setToolValue(ItemStack stack, int value, String valueName) {
        stack.getOrCreateTag().putInt(valueName, value);
    }

    public static int getToolValue(ItemStack stack, String valueName) {
        return stack.getOrCreateTag().getInt(valueName);
    }

    public static boolean getOverlay(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (tag.contains(NBTKeys.GADGET_OVERLAY))
            return tag.getBoolean(NBTKeys.GADGET_OVERLAY);

        tag.putBoolean(NBTKeys.GADGET_OVERLAY, true);
        tag.putBoolean(NBTKeys.GADGET_FUZZY, true); // We want a Destruction Gadget to start with fuzzy=true
        return true;
    }

    public static void setOverlay(ItemStack stack, boolean showOverlay) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_OVERLAY, showOverlay);
    }

    public static void switchOverlay(PlayerEntity player, ItemStack stack) {
        boolean newOverlay = ! getOverlay(stack);
        setOverlay(stack, newOverlay);
        player.displayClientMessage(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                .componentTranslation(newOverlay).setStyle(Styles.AQUA), true);
    }

    public static boolean getIsFluidOnly(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_FLUID_ONLY);
    }

    public static void toggleFluidMode(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_FLUID_ONLY, !getIsFluidOnly(stack));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);

        if (!world.isClientSide) {
            if (! player.isShiftKeyDown()) {
                BlockPos anchorPos = getAnchor(stack);
                Direction anchorSide = getAnchorSide(stack);
                if (anchorPos != null && anchorSide != null) {
                    clearArea(world, anchorPos, anchorSide, (ServerPlayerEntity) player, stack);
                    onAnchorRemoved(stack, player);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
                if (! world.isEmptyBlock(lookingAt.getBlockPos())) {
                    clearArea(world, lookingAt.getBlockPos(), lookingAt.getDirection(), (ServerPlayerEntity) player, stack);
                    onAnchorRemoved(stack, player);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                return new ActionResult<>(ActionResultType.FAIL, stack);
            }
        } else if (player.isShiftKeyDown()) {
            GuiMod.DESTRUCTION.openScreen(player);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    protected void onAnchorSet(ItemStack stack, PlayerEntity player, BlockRayTraceResult lookingAt) {
        super.onAnchorSet(stack, player, lookingAt);
        setAnchorSide(stack, lookingAt.getDirection());
    }

    @Override
    protected void onAnchorRemoved(ItemStack stack, PlayerEntity player) {
        super.onAnchorRemoved(stack, player);
        setAnchorSide(stack, null);
    }

    public static List<BlockPos> getArea(World world, BlockPos pos, Direction incomingSide, PlayerEntity player, ItemStack stack) {
        ItemStack tool = getGadget(player);
        int depth = getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH);

        if (tool.isEmpty() || depth == 0 || ! player.mayBuild())
            return new ArrayList<>();

        boolean vertical = incomingSide.getAxis().isVertical();
        Direction up = vertical ? player.getDirection() : Direction.UP;
        Direction down = up.getOpposite();
        Direction right = vertical ? up.getClockWise() : incomingSide.getCounterClockWise();
        Direction left = right.getOpposite();

        BlockPos first = pos.relative(left, getToolValue(stack, NBTKeys.GADGET_VALUE_LEFT)).relative(up, getToolValue(stack, NBTKeys.GADGET_VALUE_UP));
        BlockPos second = pos.relative(right, getToolValue(stack, NBTKeys.GADGET_VALUE_RIGHT))
                .relative(down, getToolValue(stack, NBTKeys.GADGET_VALUE_DOWN))
                .relative(incomingSide.getOpposite(), depth - 1);

        boolean isFluidOnly = getIsFluidOnly(stack);
        return new Region(first, second).stream()
                .filter(e ->
                        isFluidOnly
                            ? isFluidBlock(world, e)
                            : isValidBlock(world, e, player, world.getBlockState(e))
                )
                .sorted(Comparator.comparing(player.blockPosition()::distSqr))
                .collect(Collectors.toList());
    }

    public static boolean isFluidBlock(World world, BlockPos pos) {
        if (world.getFluidState(pos).isEmpty()) {
            return false;
        }

        return ForgeRegistries.FLUIDS.containsKey(world.getBlockState(pos).getBlock().getRegistryName());
    }

    public static boolean isValidBlock(World world, BlockPos voidPos, PlayerEntity player, BlockState currentBlock) {
        if (world.isEmptyBlock(voidPos) ||
                currentBlock.equals(OurBlocks.EFFECT_BLOCK.get().defaultBlockState()) ||
                currentBlock.getDestroySpeed(world, voidPos) < 0 ||
                ! world.mayInteract(player, voidPos)) return false;

        TileEntity te = world.getBlockEntity(voidPos);
        if ((te != null) && ! (te instanceof ConstructionBlockTileEntity))
            return false;

        if (! world.isClientSide) {
            BlockSnapshot blockSnapshot = BlockSnapshot.create(world.dimension(), world, voidPos);
            if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP))
                return false;
            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, voidPos, currentBlock, player);
            return ! MinecraftForge.EVENT_BUS.post(e);
        }
        return true;
    }

    public void clearArea(World world, BlockPos pos, Direction side, ServerPlayerEntity player, ItemStack stack) {
        List<BlockPos> positions = getArea(world, pos, side, player, stack);
        Undo.Builder builder = Undo.builder();

        for (BlockPos clearPos : positions) {
            BlockState state = world.getBlockState(clearPos);
            TileEntity te = world.getBlockEntity(clearPos);
            if (!isAllowedBlock(state.getBlock()))
                continue;
            if (te == null || state.getBlock() == OurBlocks.CONSTRUCTION_BLOCK.get() && te instanceof ConstructionBlockTileEntity) {
                destroyBlock(world, clearPos, player, builder);
            }
        }

        pushUndo(stack, builder.build(world));
    }

    private boolean destroyBlock(World world, BlockPos voidPos, ServerPlayerEntity player, Undo.Builder builder) {
        if (world.isEmptyBlock(voidPos))
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

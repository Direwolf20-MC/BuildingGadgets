package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.DestructionRender;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
import com.direwolf20.buildinggadgets.common.util.exceptions.PaletteOverflowException;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.SetBackedPlacementSequence;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GadgetDestruction extends AbstractGadget {
    private static final DestructionRender render = new DestructionRender();

    public GadgetDestruction(Properties builder) {
        super(builder);
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
    public BaseRenderer getRender() {
        return render;
    }


    private int getCostMultiplier(ItemStack tool) {
        return (int) (! getFuzzy(tool) ? Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyMultiplier.get() : 1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
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
        addEnergyInformation(tooltip, stack);
    }

    public static UUID getUUID(ItemStack stack) {
        CompoundNBT tag = NBTHelper.getOrNewTag(stack);
        if (! tag.hasUniqueId(NBTKeys.GADGET_UUID)) {
            UUID uuid = UUID.randomUUID();
            tag.putUniqueId(NBTKeys.GADGET_UUID, uuid);
            stack.setTag(tag);
            return uuid;
        }
        return tag.getUniqueId(NBTKeys.GADGET_UUID);
    }

    public static void setAnchor(ItemStack stack, BlockPos pos) {
        GadgetUtils.writePOSToNBT(stack, pos, NBTKeys.GADGET_ANCHOR);
    }

    public static BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
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
                    clearSuccess(stack);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
                if (lookingAt != null && (world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) != Blocks.AIR.getDefaultState())) {
                    clearArea(world, lookingAt.getPos(), lookingAt.getFace(), (ServerPlayerEntity) player, stack);
                    clearSuccess(stack);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }

                return new ActionResult<>(ActionResultType.FAIL, stack);
            }
        } else if (player.isSneaking()) {
            GuiMod.DESTRUCTION.openScreen(player);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    public static void clearSuccess(ItemStack stack) {
        setAnchor(stack, null);
        setAnchorSide(stack, null);
    }

    public static void anchorBlocks(PlayerEntity player, ItemStack stack) {
        BlockPos currentAnchor = getAnchor(stack);
        if (currentAnchor == null) {
            BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if (lookingAt == null || (player.world.getBlockState(VectorHelper.getLookingAt(player, stack).getPos()) == Blocks.AIR.getDefaultState())) {
                return;
            }
            currentAnchor = lookingAt.getPos();
            setAnchor(stack, currentAnchor);
            setAnchorSide(stack, lookingAt.getFace());
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {
            setAnchor(stack, null);
            setAnchorSide(stack, null);
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
    }

    public static IPositionPlacementSequence getClearingPositions(World world, BlockPos pos, Direction incomingSide, PlayerEntity player, ItemStack stack) {
        Region boundary = getClearingRegion(pos, incomingSide, player, stack);
        BlockPos startPos = (getAnchor(stack) == null) ? pos : getAnchor(stack);
        BlockState stateTarget = !Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get() || AbstractGadget.getFuzzy(stack) ? null : world.getBlockState(pos);

        if (AbstractGadget.getConnectedArea(stack)) {
            Set<BlockPos> voidPositions = new HashSet<>();
            int depth = getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH);
            if (depth == 0)
                return new SetBackedPlacementSequence(voidPositions, boundary);

            addConnectedCoordinates(world, player, startPos, stateTarget, voidPositions, boundary);
            return new SetBackedPlacementSequence(voidPositions, boundary);
        }

        return new SetBackedPlacementSequence(boundary.stream()
                .filter(p -> isValidBlock(world, p, player, stateTarget))
                .collect(Collectors.toCollection(HashSet::new)), boundary);
    }

    public static List<BlockPos> getClearingPositionsForRendering(World world, BlockPos pos, Direction incomingSide, PlayerEntity player, ItemStack stack) {
        List<BlockPos> list = getClearingPositions(world, pos, incomingSide, player, stack).stream()
                .collect(Collectors.toCollection(ArrayList::new));
        return SortingHelper.Blocks.byDistance(list, player);
    }

    private static void addConnectedCoordinates(World world, PlayerEntity player, BlockPos pos, BlockState state, Set<BlockPos> coords, Region boundary) {
        if (! boundary.contains(pos) || coords.contains(pos))
            return;
        if (! isValidBlock(world, pos, player, state))
            return;

        coords.add(pos);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoordinates(world, player, pos.add(x, y, z), state, coords, boundary);
                }
            }
        }
    }

    public static boolean isValidBlock(World world, BlockPos voidPos, PlayerEntity player, @Nullable BlockState stateTarget) {
        BlockState currentBlock = world.getBlockState(voidPos);
        if (stateTarget != null && currentBlock != stateTarget) return false;
        TileEntity te = world.getTileEntity(voidPos);

        if (currentBlock.getBlock().isAir(currentBlock, world, voidPos)) return false;
        if (currentBlock.equals(BGBlocks.effectBlock.getDefaultState())) return false;
        if ((te != null) && !(te instanceof ConstructionBlockTileEntity)) return false;
        if (currentBlock.getBlockHardness(world, voidPos) < 0) return false;

        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

        if (! player.isAllowEdit())
            return false;

        if (! world.isBlockModifiable(player, voidPos))
            return false;

        if (!world.isRemote) {
            BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, voidPos);
            if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP)) {
                return false;
            }
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
        RegionSnapshot snapshot;
        try {
            snapshot = RegionSnapshot.select(world, positions)
                    .excludeAir()
                    .checkBlocks((p, state) -> destroyBlock(world, p, player))
                    .checkTiles((p, state, tile) -> state.getBlock() == BGBlocks.constructionBlock && tile instanceof ConstructionBlockTileEntity)
                    .build();
        } catch (PaletteOverflowException e) {
            player.sendMessage(TooltipTranslation.GADGET_PALETTE_OVERFLOW.componentTranslation());
            return;
        }

        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        worldSave.addToMap(getUUID(stack).toString(), snapshot.serialize());
    }

    public static void undo(PlayerEntity player, ItemStack stack) {
        World world = player.world;
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);

        CompoundNBT serializedSnapshot = worldSave.getCompoundFromUUID(getUUID(stack).toString());
        if (serializedSnapshot.isEmpty())
            return;

        RegionSnapshot snapshot = RegionSnapshot.deserialize(serializedSnapshot);
        restoreSnapshotWithBuilder(world, snapshot);
        worldSave.addToMap(getUUID(stack).toString(), new CompoundNBT());
        worldSave.markDirty();
    }

    public static void restoreSnapshotWithBuilder(World world, RegionSnapshot snapshot) {
        Set<BlockPos> pastePositions = snapshot.getTileData().stream()
                .map(Pair::getLeft)
                .collect(Collectors.toSet());
        int index = 0;
        for (BlockPos pos : snapshot.getPositions()) {
            snapshot.getBlockStates().get(index).ifPresent(state -> EffectBlock.spawnEffectBlock(world, pos, new BlockData(state, TileSupport.dummyTileEntityData()), EffectBlock.Mode.PLACE, pastePositions.contains(pos)));
            index++;
        }
    }

    private boolean destroyBlock(World world, BlockPos voidPos, ServerPlayerEntity player) {
        if (world.isAirBlock(voidPos))
            return false;

        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

        if (! this.canUse(tool, player))
            return false;

        this.applyDamage(tool, player);
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

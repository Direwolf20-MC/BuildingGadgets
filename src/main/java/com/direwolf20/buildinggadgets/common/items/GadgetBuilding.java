package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.client.renders.BuildRender;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.items.modes.BuildingModes;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;
import com.google.common.collect.ImmutableMultiset;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetBuilding extends AbstractGadget {

    private static final MockBuilderWorld fakeWorld = new MockBuilderWorld();

    public GadgetBuilding() {
        super(OurItems.nonStackableItemProperties(),
                Config.GADGETS.GADGET_BUILDING.undoSize::get,
                Reference.SaveReference.UNDO_BUILDING,
                TagReference.WHITELIST_BUILDING,
                TagReference.BLACKLIST_BUILDING);
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
        return () -> new BuildRender(false);
    }

    public boolean placeAtop(ItemStack stack) {
        return shouldPlaceAtop(stack);
    }

    private static void setToolMode(ItemStack tool, BuildingModes mode) {
        //Store the tool's mode in NBT as a string
        CompoundTag tagCompound = tool.getOrCreateTag();
        tagCompound.putString("mode", mode.toString());
    }

    public static BuildingModes getToolMode(ItemStack tool) {
        CompoundTag tagCompound = tool.getOrCreateTag();
        return BuildingModes.getFromName(tagCompound.getString("mode"));
    }

    public static boolean shouldPlaceAtop(ItemStack stack) {
        return !stack.getOrCreateTag().getBoolean(NBTKeys.GADGET_PLACE_INSIDE);
    }

    public static void togglePlaceAtop(Player player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(NBTKeys.GADGET_PLACE_INSIDE, shouldPlaceAtop(stack));
        player.displayClientMessage((shouldPlaceAtop(stack) ? MessageTranslation.PLACE_ATOP : MessageTranslation.PLACE_INSIDE).componentTranslation().setStyle(Styles.AQUA), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        BuildingModes mode = getToolMode(stack);
        addEnergyInformation(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_MODE
                .componentTranslation((mode == BuildingModes.SURFACE && getConnectedArea(stack)
                        ? TooltipTranslation.GADGET_CONNECTED.format(new TranslatableComponent(mode.getTranslationKey()).getString())
                        : new TranslatableComponent(mode.getTranslationKey())))
                .setStyle(Styles.AQUA));

        tooltip.add(TooltipTranslation.GADGET_BLOCK
                .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack).getState()))
                .setStyle(Styles.DK_GREEN));

        int range = getToolRange(stack);
        if (getToolMode(stack) != BuildingModes.BUILD_TO_ME)
            tooltip.add(TooltipTranslation.GADGET_RANGE
                    .componentTranslation(range, getRangeInBlocks(range, mode.getMode()))
                    .setStyle(Styles.LT_PURPLE));

        if (getToolMode(stack) == BuildingModes.SURFACE)
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                    .componentTranslation(String.valueOf(getFuzzy(stack)))
                    .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);

        tooltip.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP
                .componentTranslation(String.valueOf(shouldPlaceAtop(stack)))
                .setStyle(Styles.YELLOW));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        //On item use, if sneaking, select the block clicked on, else build -- This is called when you right click a tool NOT on a block.
        ItemStack itemstack = player.getItemInHand(hand);

        player.startUsingItem(hand);
        if (!world.isClientSide) {
            // Debug code for free energy
            //itemstack.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> e.receiveEnergy(15000000, false));
            if (player.isShiftKeyDown()) {
                InteractionResultHolder<Block> result = selectBlock(itemstack, player);
                if (!result.getResult().consumesAction()) {
                    player.displayClientMessage(MessageTranslation.INVALID_BLOCK.componentTranslation(result.getObject().getRegistryName()).setStyle(Styles.AQUA), true);
                    return super.use(world, player, hand);
                }
            } else if (player instanceof ServerPlayer) {
                build((ServerPlayer) player, itemstack);
            }
        } else {
            if (!player.isShiftKeyDown()) {
                BaseRenderer.updateInventoryCache();
            } else {
                if (Screen.hasControlDown()) {
                    PacketHandler.sendToServer(new PacketBindTool());
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    public void setMode(ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        BuildingModes mode = BuildingModes.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    public static void rangeChange(Player player, ItemStack heldItem) {
        //Called when the range change hotkey is pressed
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) != BuildingModes.SURFACE || (range % 2 == 0)) ? 1 : 2;
        if (player.isShiftKeyDown())
            range = (range == 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        else
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;

        setToolRange(heldItem, range);
        player.displayClientMessage(MessageTranslation.RANGE_SET.componentTranslation(range).setStyle(Styles.AQUA), true);
    }

    private void build(ServerPlayer player, ItemStack stack) {
        //Build the blocks as shown in the visual render
        Level world = player.level;
        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return;

        List<BlockPos> coords = GadgetUtils.getAnchor(heldItem).orElse(new ArrayList<>());

        BlockData blockData = getToolBlock(heldItem);
        if (blockData.getState() == Blocks.AIR.defaultBlockState()) {
            return;
        }

        if (coords.size() == 0) {  //If we don't have an anchor, build in the current spot
            BlockHitResult lookingAt = VectorHelper.getLookingAt(player, stack);
            if (world.isEmptyBlock(lookingAt.getBlockPos())) //If we aren't looking at anything, exit
                return;

            Direction sideHit = lookingAt.getDirection();
            coords = getToolMode(stack).getMode().getCollection(
                    new AbstractMode.UseContext(world, player, blockData.getState(), lookingAt.getBlockPos(), heldItem, sideHit, placeAtop(stack), getConnectedArea(stack)),
                    player
            );
        } else  //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack);

        Undo.Builder builder = Undo.builder();
        IItemIndex index = InventoryHelper.index(stack, player);

        //TODO replace with a better TileEntity supporting Fake IWorld
        fakeWorld.setWorldAndState(player.level, blockData.getState(), coords); // Initialize the fake world's blocks
        for (BlockPos coordinate : coords) {
            //Get the extended block state in the fake world
            //Disabled to fix Chisel
            //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
            placeBlock(world, player, index, builder, coordinate, blockData);
        }

        pushUndo(stack, builder.build(world));
    }

    private void placeBlock(Level world, ServerPlayer player, IItemIndex index, Undo.Builder builder, BlockPos pos, BlockData setBlock) {
        if ((pos.getY() > world.getMaxBuildHeight() || pos.getY() < world.getMinBuildHeight()) || !player.mayBuild())
            return;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return;

        boolean useConstructionPaste = false;

        BuildContext buildContext = new BuildContext(world, player, heldItem);
        MaterialList requiredItems = setBlock.getRequiredItems(buildContext, null, pos);

        // #majorcode
        MatchResult match = index.tryMatch(requiredItems);
        if (!match.isSuccess()) {
            if (setBlock.getState().hasBlockEntity())
                return;
            match = index.tryMatch(InventoryHelper.PASTE_LIST);
            if (!match.isSuccess())
                return;
            else
                useConstructionPaste = true;
        }

        BlockSnapshot blockSnapshot = BlockSnapshot.create(world.dimension(), world, pos);
        if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP) || !world.mayInteract(player, pos) || !this.canUse(heldItem, player) || !setBlock.getState().canSurvive(world, pos))
            return;

        this.applyDamage(heldItem, player);

        if (index.applyMatch(match)) {
            ImmutableMultiset<IUniqueObject<?>> usedItems = match.getChosenOption();
            builder.record(world, pos, setBlock, usedItems, ImmutableMultiset.of());
            EffectBlock.spawnEffectBlock(world, pos, setBlock, EffectBlock.Mode.PLACE, useConstructionPaste);
        }
    }

    public static ItemStack getGadget(Player player) {
        ItemStack stack = AbstractGadget.getGadget(player);
        if (!(stack.getItem() instanceof GadgetBuilding))
            return ItemStack.EMPTY;
        return stack;
    }

    @Override
    public boolean performRotate(ItemStack stack, Player player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, PacketRotateMirror.Operation.ROTATE);
        return true;
    }

    @Override
    public boolean performMirror(ItemStack stack, Player player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, PacketRotateMirror.Operation.MIRROR);
        return true;
    }
}

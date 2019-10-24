package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.modes.ExchangingMode;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.BaseRenderer;
import com.direwolf20.buildinggadgets.common.items.gadgets.renderers.ExchangerRender;
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
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import com.google.common.collect.ImmutableMultiset;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetExchanger extends ModeGadget {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public GadgetExchanger(Properties builder, IntSupplier undoLengthSupplier, String undoName) {
        super(builder, undoLengthSupplier, undoName, TagReference.WHITELIST_EXCHANGING, TagReference.BLACKLIST_EXCHANGING);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_EXCHANGER.maxEnergy.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_EXCHANGER.energyCost.get();
    }

    @Override
    protected Supplier<BaseRenderer> createRenderFactory() {
        return ExchangerRender::new;
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
        return EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SILK_TOUCH) || super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.SILK_TOUCH || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    private static void setToolMode(ItemStack tool, ExchangingMode mode) {
        //Store the tool's mode in NBT as a string
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(tool);
        tagCompound.putString("mode", mode.getRegistryName());
    }

    public static ExchangingMode getToolMode(ItemStack tool) {
        CompoundNBT tagCompound = NBTHelper.getOrNewTag(tool);
        return ExchangingMode.byName(tagCompound.getString("mode"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        ExchangingMode mode = getToolMode(stack);
        tooltip.add(TooltipTranslation.GADGET_MODE
                .componentTranslation((mode == ExchangingMode.SURFACE && getConnectedArea(stack) ? TooltipTranslation.GADGET_CONNECTED
                        .format(mode) : mode))
                .setStyle(Styles.AQUA));
        addEnergyInformation(tooltip, stack);
        tooltip.add(TooltipTranslation.GADGET_BLOCK
                .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack).getState()))
                            .setStyle(Styles.DK_GREEN));
        tooltip.add(TooltipTranslation.GADGET_RANGE
                            .componentTranslation(getToolRange(stack))
                            .setStyle(Styles.LT_PURPLE));
        tooltip.add(TooltipTranslation.GADGET_FUZZY
                            .componentTranslation(String.valueOf(getFuzzy(stack)))
                            .setStyle(Styles.GOLD));
        addInformationRayTraceFluid(tooltip, stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                //Remove debug code
                //EnergyUtil.getCap(itemstack).ifPresent(energy -> energy.receiveEnergy(105000, false));
                selectBlock(itemstack, player);
            } else if (player instanceof ServerPlayerEntity) {
                exchange((ServerPlayerEntity) player, itemstack);
            }
        } else {
            if (! player.isSneaking()) {
                BaseRenderer.updateInventoryCache();
            } else {
                if (Screen.hasControlDown()) {
                    PacketHandler.sendToServer(new PacketBindTool());
                }
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

    public void setMode(ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        ExchangingMode mode = ExchangingMode.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    public static void rangeChange(PlayerEntity player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) == ExchangingMode.GRID || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking()) {
            range = (range <= 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        } else {
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(MessageTranslation.RANGE_SET.componentTranslation(range).setStyle(Styles.AQUA), true);
    }

    private boolean exchange(ServerPlayerEntity player, ItemStack stack) {
        ServerWorld world = player.getServerWorld();
        List<BlockPos> coords = GadgetUtils.getAnchor(stack);

        if (coords.size() == 0) { //If we don't have an anchor, build in the current spot
            BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
            BlockPos startBlock = lookingAt.getPos();
            Direction sideHit = lookingAt.getFace();
//            BlockState setBlock = getToolBlock(stack);
            coords = ExchangingMode.collectPlacementPos(world, player, startBlock, sideHit, stack, startBlock);
        } else { //If we do have an anchor, erase it (Even if the build fails)
            setAnchor(stack, new ArrayList<BlockPos>());
        }
        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return false;

        BlockData blockState = getToolBlock(heldItem);
        Undo.Builder builder = Undo.builder();
        IItemIndex index = InventoryHelper.index(stack, player);
        if (blockState.getState() != Blocks.AIR.getDefaultState()) {  //Don't attempt a build if a block is not chosen -- Typically only happens on a new tool.
            //TODO replace fakeWorld
            fakeWorld.setWorldAndState(player.world, blockState.getState(), coordinates); // Initialize the fake world's blocks
            for (BlockPos coordinate : coords) {
                //Get the extended block state in the fake world
                //Disabled to fix Chisel
                //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                exchangeBlock(world, player, index, builder, coordinate, blockState);
            }
        }
        pushUndo(stack, builder.build(world.getDimension().getType()));
        return true;
    }

    private boolean exchangeBlock(ServerWorld world, ServerPlayerEntity player, IItemIndex index, Undo.Builder builder, BlockPos pos, BlockData setBlock) {
        BlockState currentBlock = world.getBlockState(pos);
        //ItemStack itemStack = setBlock.getBlock().getPickBlock(setBlock, null, world, pos, player);


        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;


        IBuildContext buildContext = SimpleBuildContext.builder()
                .usedStack(tool)
                .buildingPlayer(player)
                .build(world);
        MaterialList requiredItems = setBlock.getRequiredItems(buildContext, null, pos);
        MatchResult match = index.tryMatch(requiredItems);
        boolean useConstructionPaste = false;
        if (! match.isSuccess()) {
            if (setBlock.getState().hasTileEntity())
                return false;
            match = index.tryMatch(InventoryHelper.PASTE_LIST);
            if (! match.isSuccess())
                return false;
            else
                useConstructionPaste = true;
        }
        if (! player.isAllowEdit())
            return false;
        if (! world.isBlockModifiable(player, pos))
            return false;
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP))
            return false;
        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, pos, currentBlock, player);
        if (MinecraftForge.EVENT_BUS.post(e)) {
            return false;
        }

        if (!this.canUse(tool, player))
            return false;

        this.applyDamage(tool, player);

//        currentBlock.getBlock().harvestBlock(world, player, pos, currentBlock, world.getTileEntity(pos), tool);


        if (index.applyMatch(match)) {
            ImmutableMultiset<IUniqueObject<?>> usedItems = match.getChosenOption();
            MaterialList materials = TileSupport.createTileData(world, pos).getRequiredItems(
                    buildContext,
                    currentBlock,
                    world.rayTraceBlocks(new RayTraceContext(player.getPositionVec(), new Vec3d(pos), BlockMode.COLLIDER, FluidMode.NONE, player)),
                    pos);
            Iterator<ImmutableMultiset<IUniqueObject<?>>> it = materials.iterator();
            ImmutableMultiset<IUniqueObject<?>> producedItems = it.hasNext() ? it.next() : ImmutableMultiset.of();
            index.insert(producedItems);
            builder.record(world, pos, setBlock, usedItems, producedItems);
            EffectBlock.spawnEffectBlock(world, pos, setBlock, EffectBlock.Mode.REPLACE, useConstructionPaste);
            return true;
        }
        return false;
    }

    public static ItemStack getGadget(PlayerEntity player) {
        ItemStack stack = AbstractGadget.getGadget(player);
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

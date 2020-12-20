package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.client.renders.BuildRender;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.items.modes.BuildingModes;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.items.modes.ExchangingModes;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBindTool;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference.TagReference;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.Block;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.*;

public class GadgetExchanger extends AbstractGadget {
    private static final MockBuilderWorld fakeWorld = new MockBuilderWorld();

    public GadgetExchanger() {
        super(OurItems.nonStackableItemProperties(),
                () -> 0,
                "",
                TagReference.WHITELIST_EXCHANGING,
                TagReference.BLACKLIST_EXCHANGING);
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
        return () -> new BuildRender(true);
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

    private static void setToolMode(ItemStack tool, ExchangingModes mode) {
        //Store the tool's mode in NBT as a string
        CompoundNBT tagCompound = tool.getOrCreateTag();
        tagCompound.putString("mode", mode.toString());
    }

    public static ExchangingModes getToolMode(ItemStack tool) {
        CompoundNBT tagCompound = tool.getOrCreateTag();
        return ExchangingModes.getFromName(tagCompound.getString("mode"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        addEnergyInformation(tooltip, stack);

        ExchangingModes mode = getToolMode(stack);
        tooltip.add(TooltipTranslation.GADGET_MODE
                .componentTranslation((mode == ExchangingModes.SURFACE && getConnectedArea(stack)
                        ? TooltipTranslation.GADGET_CONNECTED.format(new TranslationTextComponent(mode.getTranslationKey()).getString())
                        : new TranslationTextComponent(mode.getTranslationKey())))
                .setStyle(Styles.AQUA));

        tooltip.add(TooltipTranslation.GADGET_BLOCK
                .componentTranslation(LangUtil.getFormattedBlockName(getToolBlock(stack).getState()))
                            .setStyle(Styles.DK_GREEN));

        int range = getToolRange(stack);
        tooltip.add(TooltipTranslation.GADGET_RANGE
                            .componentTranslation(range, getRangeInBlocks(range, mode.getMode()))
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
                ActionResult<Block> result = selectBlock(itemstack, player);
                if( !result.getType().isSuccessOrConsume() ) {
                    player.sendStatusMessage(MessageTranslation.INVALID_BLOCK.componentTranslation(result.getResult().getRegistryName()).setStyle(Styles.AQUA), true);
                    return super.onItemRightClick(world, player, hand);
                }
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
        ExchangingModes mode = ExchangingModes.values()[modeInt];
        setToolMode(heldItem, mode);
    }

    public static void rangeChange(PlayerEntity player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        int changeAmount = (getToolMode(heldItem) == ExchangingModes.GRID || (range % 2 == 0)) ? 1 : 2;
        if (player.isSneaking()) {
            range = (range <= 1) ? Config.GADGETS.maxRange.get() : range - changeAmount;
        } else {
            range = (range >= Config.GADGETS.maxRange.get()) ? 1 : range + changeAmount;
        }
        setToolRange(heldItem, range);
        player.sendStatusMessage(MessageTranslation.RANGE_SET.componentTranslation(range).setStyle(Styles.AQUA), true);
    }

    private void exchange(ServerPlayerEntity player, ItemStack stack) {
        ServerWorld world = player.getServerWorld();
        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return;

        BlockData blockData = getToolBlock(heldItem);

        // Don't attempt to do anything if we can't actually do it.
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
        TileEntity tileEntity = world.getTileEntity(lookingAt.getPos());
        Block lookAtBlock = player.world.getBlockState(lookingAt.getPos()).getBlock();
        if (blockData.getState() == Blocks.AIR.getDefaultState()
                || lookAtBlock == OurBlocks.EFFECT_BLOCK.get()
                || blockData.getState().getBlock() == lookAtBlock
                || tileEntity != null) {
            return;
        }

        // Get the anchor or build the collection
        Optional<List<BlockPos>> anchor = GadgetUtils.getAnchor(stack);
        List<BlockPos> coords = anchor.orElseGet(
                () -> getToolMode(stack).getMode().getCollection(new AbstractMode.UseContext(world, blockData.getState(), lookingAt.getPos(), heldItem, lookingAt.getFace(), getConnectedArea(heldItem)), player)
        );

        IItemIndex index = InventoryHelper.index(stack, player);

        //TODO replace fakeWorld
        fakeWorld.setWorldAndState(player.world, blockData.getState(), coords); // Initialize the fake world's blocks
        for (BlockPos coordinate : coords) {
            //Get the extended block state in the fake world Disabled to fix Chisel
            //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
            exchangeBlock(world, player, index, coordinate, blockData);
        }
    }

    private void exchangeBlock(ServerWorld world, ServerPlayerEntity player, IItemIndex index, BlockPos pos, BlockData setBlock) {
        BlockState currentBlock = world.getBlockState(pos);
        ITileEntityData data;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            data = ((ConstructionBlockTileEntity) te).getConstructionBlockData().getTileData();
            currentBlock = ((ConstructionBlockTileEntity) te).getConstructionBlockData().getState();
        } else
            data = TileSupport.createTileData(world, pos);

        ItemStack tool = getGadget(player);
        if (tool.isEmpty() || !this.canUse(tool, player))
            return;

        BuildContext buildContext = BuildContext.builder()
                .stack(tool)
                .player(player)
                .build(world);

        MaterialList requiredItems = setBlock.getRequiredItems(buildContext, null, pos);
        MatchResult match = index.tryMatch(requiredItems);
        boolean useConstructionPaste = false;
        if (! match.isSuccess()) {
            if (setBlock.getState().hasTileEntity())
                return;
            match = index.tryMatch(InventoryHelper.PASTE_LIST);
            if (! match.isSuccess())
                return;
            else
                useConstructionPaste = true;
        }

        if (! player.isAllowEdit() || ! world.isBlockModifiable(player, pos))
            return;

        BlockSnapshot blockSnapshot = BlockSnapshot.create(world.getDimensionKey(), world, pos);
        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, pos, currentBlock, player);
        if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, Direction.UP) || MinecraftForge.EVENT_BUS.post(e))
            return;

        this.applyDamage(tool, player);

        if (index.applyMatch(match)) {
            MaterialList materials = te instanceof ConstructionBlockTileEntity ? InventoryHelper.PASTE_LIST : data.getRequiredItems(
                    buildContext,
                    currentBlock,
                    world.rayTraceBlocks(new RayTraceContext(player.getPositionVec(), Vector3d.copy(pos), BlockMode.COLLIDER, FluidMode.NONE, player)),
                    pos);

            Iterator<ImmutableMultiset<IUniqueObject<?>>> it = materials.iterator();
            Multiset<IUniqueObject<?>> producedItems = LinkedHashMultiset.create();

            if (buildContext.getStack().isEnchanted() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, buildContext.getStack()) > 0) {
                producedItems = it.hasNext() ? it.next() : ImmutableMultiset.of();
            } else {
                List<ItemStack> drops = Block.getDrops(currentBlock, (ServerWorld) buildContext.getWorld(), pos, buildContext.getWorld().getTileEntity(pos));
                producedItems.addAll(drops.stream().map(UniqueItem::ofStack).collect(Collectors.toList()));
            }

            index.insert(producedItems);

            EffectBlock.spawnEffectBlock(world, pos, setBlock, EffectBlock.Mode.REPLACE, useConstructionPaste);
        }
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
    public boolean performRotate(ItemStack stack, PlayerEntity player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, PacketRotateMirror.Operation.ROTATE);
        return true;
    }

    @Override
    public boolean performMirror(ItemStack stack, PlayerEntity player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, PacketRotateMirror.Operation.MIRROR);
        return true;
    }
}

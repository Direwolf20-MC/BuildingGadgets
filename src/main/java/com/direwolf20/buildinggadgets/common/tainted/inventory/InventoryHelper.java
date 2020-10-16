package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IHandleProvider;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.ItemHandlerProvider;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import java.util.*;
import java.util.function.Supplier;

/*
 * @MichaelHillcox
 *  This entire class could do with some refactoring and cleaning :grin:
 */
public class InventoryHelper {
    public static final MaterialList PASTE_LIST = MaterialList.of(new UniqueItem(OurItems.CONSTRUCTION_PASTE_ITEM.get()));

    private static final Set<Property<?>> UNSAFE_PROPERTIES =
            ImmutableSet.<Property<?>>builder()
                    .add(CropsBlock.AGE)
                    .add(BlockStateProperties.HALF)
                    .build();

    public static final CreativeItemIndex CREATIVE_INDEX = new CreativeItemIndex();

    public static IItemIndex index(ItemStack tool, PlayerEntity player) {
        if (player.isCreative())
            return CREATIVE_INDEX;
        return new PlayerItemIndex(tool, player);
    }

    static List<IInsertProvider> indexInsertProviders(ItemStack tool, PlayerEntity player) {
        ImmutableList.Builder<IInsertProvider> builder = ImmutableList.builder();

        InventoryLinker.getLinkedInventory(player.world, tool).ifPresent(e -> builder.add(new HandlerInsertProvider(e)));
        builder.add(new PlayerInventoryInsertProvider(player));

        return builder.build();
    }

    static Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> indexMap(ItemStack tool, PlayerEntity player) {
        Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> map = new HashMap<>();
        for (IItemHandler handler : getHandlers(tool, player)) {
            if (handler != null && handler.getSlots() > 0)
                ItemHandlerProvider.index(handler, map);
        }
        return map;
    }

    static List<IItemHandler> getHandlers(ItemStack stack, PlayerEntity player) {
        List<IItemHandler> handlers = new ArrayList<>();

        InventoryLinker.getLinkedInventory(player.world, stack).ifPresent(handlers::add);
        player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handlers::add);

        return handlers;
    }

    public static void registerHandleProviders() {
        InterModComms.sendTo(Reference.MODID, Reference.HandleProviderReference.IMC_METHOD_HANDLE_PROVIDER, () -> (Supplier<TopologicalRegistryBuilder<IHandleProvider>>)
                (() -> TopologicalRegistryBuilder.<IHandleProvider>create()
                        .addMarker(Reference.MARKER_AFTER_RL)
                        .addValue(Reference.HandleProviderReference.STACK_HANDLER_ITEM_HANDLE_RL, new ItemHandlerProvider())
                        .addDependency(Reference.HandleProviderReference.STACK_HANDLER_ITEM_HANDLE_RL, Reference.MARKER_AFTER_RL)));
    }

    public static boolean giveItem(ItemStack itemStack, PlayerEntity player, World world) {
        if (player.isCreative()) {
            return true;
        }
        if (itemStack.getItem() instanceof ConstructionPaste) {
            itemStack = addPasteToContainer(player, itemStack);
        }
        if (itemStack.getCount() == 0) {
            return true;
        }

        //Fill any unfilled stacks in the player's inventory first
        PlayerInventory inv = player.inventory;
        List<Integer> slots = findItem(itemStack.getItem(), inv);
        for (int slot : slots) {
            ItemStack stackInSlot = inv.getStackInSlot(slot);
            if (stackInSlot.getCount() < stackInSlot.getItem().getItemStackLimit(stackInSlot)) {
                ItemStack giveItemStack = itemStack.copy();
                boolean success = inv.addItemStackToInventory(giveItemStack);
                if (success) return true;
            }
        }

        //Try to insert into the remote inventory.
        ItemStack tool = AbstractGadget.getGadget(player);
        IItemHandler remoteInventory = InventoryLinker.getLinkedInventory(world, tool).orElse(new EmptyHandler());
        if (remoteInventory.getSlots() > 0) {
            for (int i = 0; i < remoteInventory.getSlots(); i++) {
                ItemStack containerItem = remoteInventory.getStackInSlot(i);
                ItemStack giveItemStack = itemStack.copy();
                if (containerItem.getItem() == itemStack.getItem() || containerItem.isEmpty()) {
                    giveItemStack = remoteInventory.insertItem(i, giveItemStack, false);
                    if (giveItemStack.isEmpty())
                        return true;

                    itemStack = giveItemStack.copy();
                }
            }
        }


        List<IItemHandler> invContainers = findInvContainers(inv);
        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                for (int i = 0; i < container.getSlots(); i++) {
                    ItemStack containerItem = container.getStackInSlot(i);
                    ItemStack giveItemStack = itemStack.copy();
                    if (containerItem.getItem() == giveItemStack.getItem()) {
                        giveItemStack = container.insertItem(i, giveItemStack, false);
                        if (giveItemStack.isEmpty())
                            return true;

                        itemStack = giveItemStack.copy();
                    }
                }
            }
        }
        ItemStack giveItemStack = itemStack.copy();
        return inv.addItemStackToInventory(giveItemStack);
    }

    public static ItemStack addPasteToContainer(PlayerEntity player, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ConstructionPaste)) {
            return itemStack;
        }

        PlayerInventory inv = player.inventory;
        List<Integer> slots = findItemClass(ConstructionPasteContainer.class, inv);
        if (slots.size() == 0) {
            return itemStack;
        }

        Map<Integer, Integer> slotMap = new HashMap<>();
        for (int slot : slots) {
            slotMap.put(slot, ConstructionPasteContainer.getPasteAmount(inv.getStackInSlot(slot)));
        }

        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(slotMap.entrySet());
        Comparator<Map.Entry<Integer, Integer>> comparator = Map.Entry.comparingByValue();
        comparator = comparator.reversed();
        list.sort(comparator);


        for (Map.Entry<Integer, Integer> entry : list) {
            ItemStack containerStack = inv.getStackInSlot(entry.getKey());
            ConstructionPasteContainer item = ((ConstructionPasteContainer) containerStack.getItem());

            int maxAmount = item.getMaxCapacity();
            int pasteInContainer = ConstructionPasteContainer.getPasteAmount(containerStack);
            int freeSpace = item.isCreative() ? Integer.MAX_VALUE : maxAmount - pasteInContainer;
            int stackSize = itemStack.getCount();

            int remainingPaste = stackSize - freeSpace;
            if (remainingPaste < 0) {
                remainingPaste = 0;
            }

            int usedPaste = Math.abs(stackSize - remainingPaste);
            itemStack.setCount(remainingPaste);
            ConstructionPasteContainer.setPasteAmount(containerStack, pasteInContainer + usedPaste);
        }

        return itemStack;
    }

    private static List<IItemHandler> findInvContainers(PlayerInventory inv) {
        List<IItemHandler> containers = new ArrayList<>();

        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(containers::add);
        }

        return containers;
    }

    public static int countInContainer(IItemHandler container, Item item) {
        int count = 0;
        ItemStack tempItem;
        for (int i = 0; i < container.getSlots(); ++i) {
            tempItem = container.getStackInSlot(i);
            if (tempItem.getItem() == item) {
                count += tempItem.getCount();
            }
        }
        return count;
    }

    private static List<Integer> findItem(Item item, PlayerInventory inv) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static List<Integer> findItemClass(Class<?> c, PlayerInventory inv) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && c.isInstance(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static ItemStack getSilkTouchDrop(BlockState state) {
        return new ItemStack(state.getBlock());
    }

    public static Optional<BlockData> getSafeBlockData(PlayerEntity player, BlockPos pos, Hand hand) {
        BlockItemUseContext blockItemUseContext = new BlockItemUseContext(new ItemUseContext(player, hand, CommonUtils.fakeRayTrace(player.getPositionVec(), pos)));
        return getSafeBlockData(player, pos, blockItemUseContext);
    }

    public static Optional<BlockData> getSafeBlockData(PlayerEntity player, BlockPos pos, BlockItemUseContext useContext) {
        World world = player.world;
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof FlowingFluidBlock || ! state.getFluidState().isEmpty() )
            return Optional.empty();
        if (state.getBlock() == OurBlocks.CONSTRUCTION_BLOCK.get()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ConstructionBlockTileEntity) //should already be checked
                return Optional.of(((ConstructionBlockTileEntity) te).getConstructionBlockData());
        }
        BlockState placeState = null;
        try {
            placeState = state.getBlock().getStateForPlacement(useContext);
        } catch (Exception e) {
            ; //this can happen if the context doesn't match how it should be
        }
        if (placeState == null)
            placeState = state.getBlock().getDefaultState();
        for (Property<?> prop : placeState.getProperties()) {
            if (! UNSAFE_PROPERTIES.contains(prop))
                placeState = applyProperty(placeState, state, prop);
        }

        return Optional.of(new BlockData(placeState, TileSupport.createTileData(world, pos)));
    }

    //proper generics...
    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, BlockState from, Property<T> prop) {
        return state.with(prop, from.get(prop));
    }
}

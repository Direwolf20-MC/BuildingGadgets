package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.APIReference;
import com.direwolf20.buildinggadgets.api.APIReference.HandleProviderReference;
import com.direwolf20.buildinggadgets.api.Registries.HandleProvider;
import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.inventory.IHandleProvider;
import com.direwolf20.buildinggadgets.api.materials.inventory.IObjectHandle;
import com.direwolf20.buildinggadgets.api.materials.inventory.UniqueItem;
import com.direwolf20.buildinggadgets.api.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.pastes.GenericPasteContainer;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.function.Supplier;

/*
 * @MichaelHillcox
 *  This entire class could do with some refactoring and cleaning :grin:
 */
public class InventoryHelper {
    public static final MaterialList PASTE_LIST = MaterialList.of(new UniqueItem(OurItems.constructionPaste));
    private static IProperty AXIS = EnumProperty.create("axis", Direction.Axis.class);

    private static final Set<IProperty> SAFE_PROPERTIES =
            ImmutableSet.of(SlabBlock.TYPE, StairsBlock.HALF, LogBlock.AXIS, AXIS, DirectionalBlock.FACING, StairsBlock.FACING, TrapDoorBlock.HALF, TrapDoorBlock.OPEN, StairsBlock.SHAPE, LeverBlock.POWERED, RepeaterBlock.DELAY, PaneBlock.EAST, PaneBlock.WEST, PaneBlock.NORTH, PaneBlock.SOUTH);

    private static final Set<IProperty> SAFE_PROPERTIES_COPY_PASTE =
            ImmutableSet.<IProperty>builder().addAll(SAFE_PROPERTIES).addAll(ImmutableSet.of(RailBlock.SHAPE, PoweredRailBlock.SHAPE, ChestBlock.TYPE)).build();
    public static final CreativeItemIndex CREATIVE_INDEX = new CreativeItemIndex();

    public static IItemIndex index(ItemStack tool, PlayerEntity player) {
        if (player.isCreative())
            return CREATIVE_INDEX;
        return new PlayerItemIndex(tool, player);
    }

    static List<IInsertProvider> indexInsertProviders(ItemStack tool, PlayerEntity player) {
        ImmutableList.Builder<IInsertProvider> builder = ImmutableList.builder();
        IItemHandler remoteInv = GadgetUtils.getRemoteInventory(tool, player.world);
        if (remoteInv != null)
            builder.add(new HandlerInsertProvider(remoteInv));
        builder.add(new PlayerInventoryInsertProvider(player));
        return builder.build();
    }

    static Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> indexMap(ItemStack tool, PlayerEntity player) {
        Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> map = new HashMap<>();
        for (IItemHandler handler : getHandlers(tool, player)) {
            if (handler != null)
                ItemHandlerProvider.index(handler, map);
        }
        return map;
    }

    static List<IItemHandler> getHandlers(ItemStack stack, PlayerEntity player) {
        return Arrays.asList(
                GadgetUtils.getRemoteInventory(stack, player.world),
                player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(CapabilityNotPresentException::new));
    }

    private static void indexHandler(Multimap<Item, IObjectHandle> map, IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (! stack.isEmpty()) {
                Set<Class<?>> testedClasses = null;
                for (IHandleProvider provider : HandleProvider.getHandleProviders().getValuesInOrder()) {

                }
            }
        }
    }

    public static void registerHandleProviders() {
        InterModComms.sendTo(Reference.MODID, HandleProviderReference.IMC_METHOD_HANDLE_PROVIDER, () -> (Supplier<TopologicalRegistryBuilder<IHandleProvider>>)
                (() -> TopologicalRegistryBuilder.<IHandleProvider>create()
                        .addMarker(APIReference.MARKER_AFTER_RL)
                        .addValue(HandleProviderReference.STACK_HANDLER_ITEM_HANDLE_RL, new ItemHandlerProvider())
                        .addDependency(HandleProviderReference.STACK_HANDLER_ITEM_HANDLE_RL, APIReference.MARKER_AFTER_RL)));
    }

    public static boolean giveItem(ItemStack itemStack, PlayerEntity player, IWorld world) {
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
        IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(tool,world.getWorld());
        if (remoteInventory != null) {
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

    public static boolean useItem(ItemStack itemStack, PlayerEntity player, int count, World world) {
        if (player.isCreative()) {
            return true;
        }

        ItemStack tool = AbstractGadget.getGadget(player);
        IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(tool, world);
        if (remoteInventory != null) {
            for (int i = 0; i < remoteInventory.getSlots(); i++) {
                ItemStack containerItem = remoteInventory.getStackInSlot(i);
                if (containerItem.getItem() == itemStack.getItem() && containerItem.getCount() >= count) {
                    remoteInventory.extractItem(i, count, false);
                    return true;
                }
            }
        }


        PlayerInventory inv = player.inventory;

        List<Integer> slots = findItem(itemStack.getItem(), inv);
        List<IItemHandler> invContainers = findInvContainers(inv);

        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                for (int i = 0; i < container.getSlots(); i++) {
                    ItemStack containerItem = container.getStackInSlot(i);
                    if (containerItem.getItem() == itemStack.getItem() && containerItem.getCount() >= count) {
                        container.extractItem(i, count, false);
                        return true;
                    }
                }
            }
        }
        if (slots.size() == 0) {
            return false;
        }
        int slot = slots.get(0);
        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (stackInSlot.getCount() < count) {
            return false;
        }
        stackInSlot.shrink(count);
        return true;
    }

    public interface IRemoteInventoryProvider {
        int countItem(ItemStack tool, ItemStack stack);
    }

    public static int countItem(ItemStack itemStack, PlayerEntity player, World world) {
        return countItem(itemStack, player, (tool, stack) -> {
            IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(tool, world);
            return remoteInventory == null ? 0 : countInContainer(remoteInventory, stack.getItem());
        });
    }

    public static int countItem(ItemStack itemStack, PlayerEntity player, IRemoteInventoryProvider remoteInventory) {
        if (player.isCreative())
            return Integer.MAX_VALUE;

        long count = remoteInventory.countItem(AbstractGadget.getGadget(player), itemStack);
        PlayerInventory inv = player.inventory;
        List<Integer> slots = findItem(itemStack.getItem(), inv);
        List<IItemHandler> invContainers = findInvContainers(inv);
        if (slots.size() == 0 && invContainers.size() == 0 && count == 0) {
            return 0;
        }
        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                count += countInContainer(container, itemStack.getItem());
            }
        }

        for (int slot : slots) {
            ItemStack stackInSlot = inv.getStackInSlot(slot);
            count += stackInSlot.getCount();
        }
        return longToInt(count);
    }

    public static int countPaste(PlayerEntity player) {
        if (player.isCreative())
            return Integer.MAX_VALUE;

        long count = 0;
        PlayerInventory inv = player.inventory;
        Item item = OurItems.constructionPaste;
        List<Integer> slots = findItem(item, inv);
        if (slots.size() > 0) {
            for (int slot : slots) {
                ItemStack stackInSlot = inv.getStackInSlot(slot);
                count += stackInSlot.getCount();
            }
        }

        List<Integer> containerSlots = findItemClass(GenericPasteContainer.class, inv);
        if (containerSlots.size() > 0) {
            for (int slot : containerSlots) {
                ItemStack stackInSlot = inv.getStackInSlot(slot);
                if (stackInSlot.getItem() instanceof GenericPasteContainer) {
                    count = count + GenericPasteContainer.getPasteAmount(stackInSlot);
                }
            }
        }
        return longToInt(count);
    }

    public static int longToInt(long count)
    {
        try {
            return Math.toIntExact(count);
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
    }

    public static ItemStack addPasteToContainer(PlayerEntity player, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ConstructionPaste)) {
            return itemStack;
        }
        PlayerInventory inv = player.inventory;
        List<Integer> slots = findItemClass(GenericPasteContainer.class, inv);
        if (slots.size() == 0) {
            return itemStack;
        }

        Map<Integer, Integer> slotMap = new HashMap<>();
        for (int slot : slots) {
            slotMap.put(slot, GenericPasteContainer.getPasteAmount(inv.getStackInSlot(slot)));
        }
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(slotMap.entrySet());
        Comparator<Map.Entry<Integer, Integer>> comparator = Comparator.comparing(entry -> entry.getValue());
        comparator = comparator.reversed();
        list.sort(comparator);


        for (Map.Entry<Integer, Integer> entry : list) {
            ItemStack containerStack = inv.getStackInSlot(entry.getKey());
            GenericPasteContainer item = ((GenericPasteContainer) containerStack.getItem());
            int maxAmount = item.getMaxCapacity();
            int pasteInContainer = GenericPasteContainer.getPasteAmount(containerStack);
            int freeSpace = item instanceof ConstructionPasteContainerCreative ? Integer.MAX_VALUE : maxAmount - pasteInContainer;
            int stackSize = itemStack.getCount();
            int remainingPaste = stackSize - freeSpace;
            if (remainingPaste < 0) {
                remainingPaste = 0;
            }
            int usedPaste = Math.abs(stackSize - remainingPaste);
            itemStack.setCount(remainingPaste);
            GenericPasteContainer.setPasteAmount(containerStack, pasteInContainer + usedPaste);
        }
        return itemStack;
    }

    public static boolean usePaste(PlayerEntity player, int count) {
        if (player.isCreative()) {
            return true;
        }
        PlayerInventory inv = player.inventory;
        List<Integer> slots = findItem(OurItems.constructionPaste, inv);
        if (slots.size() > 0) {
            for (int slot : slots) {
                ItemStack pasteStack = inv.getStackInSlot(slot);
                if (pasteStack.getCount() >= count) {
                    pasteStack.shrink(count);
                    return true;
                }
            }
        }
        List<Integer> containerSlots = findItemClass(GenericPasteContainer.class, inv);
        if (containerSlots.size() > 0) {
            for (int slot : containerSlots) {
                ItemStack containerStack = inv.getStackInSlot(slot);
                if (containerStack.getItem() instanceof GenericPasteContainer) {
                    int pasteAmt = GenericPasteContainer.getPasteAmount(containerStack);
                    if (pasteAmt >= count) {
                        GenericPasteContainer.setPasteAmount(containerStack, pasteAmt - count);
                        return true;
                    }

                }
            }
        }
        return false;
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

    public static List<Integer> findItemClass(Class c, PlayerInventory inv) {
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

    public static BlockData getSpecificStates(BlockState originalState, World world, PlayerEntity player, BlockPos pos, ItemStack tool) {
        BlockState placeState;
        //Block block = originalState.getBlock();
        //ItemStack item = block.getPickBlock(originalState, null, world, pos, player);

        try {
            placeState = originalState.getBlock().getStateForPlacement(
                    new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, VectorHelper.getLookingAt(player, tool))));
        } catch (Exception var8) {
            placeState = originalState.getBlock().getDefaultState();
        }

        if (placeState != null) {
            for (IProperty prop : placeState.getProperties()) {
                if (tool.getItem() instanceof GadgetCopyPaste) {
                    if (SAFE_PROPERTIES_COPY_PASTE.contains(prop)) {
                        placeState = placeState.with(prop, originalState.get(prop));
                    }
                } else {
                    if (SAFE_PROPERTIES.contains(prop)) {
                        placeState = placeState.with(prop, originalState.get(prop));
                    }
                }
            }

            return new BlockData(placeState, TileSupport.createTileData(world, pos));
        }
        return null;
    }

    private static NonNullList<ItemStack> playerInv(PlayerEntity player) {
        NonNullList<ItemStack> wholeInv = NonNullList.from(ItemStack.EMPTY, (ItemStack[]) player.inventory.mainInventory.toArray());
        wholeInv.addAll(player.inventory.offHandInventory);
        wholeInv.addAll(player.inventory.armorInventory);
        return wholeInv;
    }
}

package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.GenericPasteContainer;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static net.minecraftforge.items.CapabilityItemHandler.*;

public class InventoryManipulation {
    private enum InventoryType {
        PLAYER, LINKED, OTHER
    }

    private static IProperty AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);
    private static final Set<IProperty> SAFE_PROPERTIES = ImmutableSet.of(BlockSlab.HALF, BlockStairs.HALF, BlockLog.LOG_AXIS, AXIS, BlockDirectional.FACING,
            BlockStairs.FACING, BlockTrapDoor.HALF, BlockTorch.FACING, BlockStairs.SHAPE, BlockLever.FACING, BlockLever.POWERED, BlockRedstoneRepeater.DELAY,
            BlockStoneSlab.VARIANT, BlockWoodSlab.VARIANT, BlockDoubleWoodSlab.VARIANT, BlockDoubleStoneSlab.VARIANT);

    private static final Set<IProperty> SAFE_PROPERTIES_COPY_PASTE = ImmutableSet.<IProperty>builder().addAll(SAFE_PROPERTIES)
            .addAll(ImmutableSet.of(BlockDoubleWoodSlab.VARIANT, BlockRail.SHAPE, BlockRailPowered.SHAPE)).build();

    public static ItemStack giveItem(ItemStack targetStack, EntityPlayer player, World world) {
        if (player.capabilities.isCreativeMode)
            return ItemStack.EMPTY;

        // Attempt to dump any construction paste back in it's container.
        ItemStack target = targetStack.getItem() instanceof ConstructionPaste ? addPasteToContainer(player, targetStack) : targetStack;
        if (target.getCount() == 0)
            return ItemStack.EMPTY;

        ItemStack tool = GadgetGeneric.getGadget(player);
        for(Pair<InventoryType, IItemHandler> inv : collectInventories(tool, player, world, NetworkIO.Operation.INSERT)) {
            target = insertIntoInventory(inv.getValue(), target, inv.getKey());
            if( target.isEmpty() )
                return ItemStack.EMPTY;
        }

        return target;
    }

    private static ItemStack insertIntoInventory(IItemHandler inventory, ItemStack target, InventoryType type) {
        if( inventory == null )
            return target;

        // First try and deposit the majority to slots that contain that item.
        for (int i = 0; i < inventory.getSlots(); i++) {
            if( target.isEmpty() )
                return target; // Return here to not run the next for loop

            ItemStack containerItem = inventory.getStackInSlot(i);
            if (!containerItem.isEmpty() && containerItem.getItem() == target.getItem() && containerItem.getMetadata() == target.getMetadata()) {
                // Chunk and calculate how much to insert per stack.
                int insertCount = (target.getCount() - containerItem.getCount()) > containerItem.getMaxStackSize() ? (target.getCount() - containerItem.getCount()) : target.getCount();
                if( containerItem.getCount() + insertCount > target.getMaxStackSize() )
                    continue;

                ItemStack insertStack = containerItem.copy();
                insertStack.setCount(insertCount);

                inventory.insertItem(i, insertStack, false);
                target.shrink(insertCount);
            }
        }

        // Finally, just dump it in any empty slots. (we'll throw it on the ground if there is still some left so don't worry about the remainder)
        for (int i = 0; i < inventory.getSlots(); i++) {
            if( target.isEmpty() )
                break;

            ItemStack containerItem = inventory.getStackInSlot(i);
            if( !containerItem.isEmpty() || !inventory.isItemValid(i, target) || type == InventoryType.PLAYER && i == 40 )
                continue;

            ItemStack insertStack = target.copy();
            insertStack.setCount(target.getCount() > target.getMaxStackSize() ? containerItem.getMaxStackSize() : target.getCount());

            ItemStack stack = inventory.insertItem(i, insertStack, true);
            if( stack.getCount() == insertStack.getCount() )
                continue;

            inventory.insertItem(i, insertStack, false);
            target.shrink(insertStack.getCount());
        }

        return target;
    }

    /**
     * Runs though each inventory to find and use the items required for the building inventory.
     * See {@link #collectInventories(ItemStack, EntityPlayer, World, NetworkIO.Operation)} to find the order of inventories returned.
     *
     * @implNote Call {@link GadgetUtils#clearCachedRemoteInventory GadgetUtils#clearCachedRemoteInventory} when done using this method
     *
     * @return boolean based on if the method was able to supply any amount of items. If the method is called requiring
     *         10 items and we only find 5 we still return true. We only return false if no items where supplied.
     *         This is by design.
     */
    public static boolean useItem(ItemStack target, EntityPlayer player, int amountRequired, World world) {
        if (player.capabilities.isCreativeMode)
            return true;

        int amountLeft = amountRequired;
        for (Pair<InventoryType, IItemHandler> inv : collectInventories(GadgetGeneric.getGadget(player), player, world, NetworkIO.Operation.EXTRACT)) {
            amountLeft -= extractFromInventory(inv.getValue(), target, amountLeft);

            if( amountLeft <= 0 )
                return true;
        }

        return amountLeft < amountRequired;
    }

    private static int extractFromInventory(IItemHandler inventory, ItemStack target, int amountRequired) {
        int amountSaturated = 0;
        if( inventory == null )
            return amountSaturated;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack containerItem = inventory.getStackInSlot(i);
            if (containerItem.getItem() == target.getItem() && containerItem.getMetadata() == target.getMetadata()) {
                ItemStack stack = inventory.extractItem(i, amountRequired, false);
                amountSaturated += stack.getCount();
            }

            // Don't continue to check if we've saturated the amount.
            if( amountSaturated >= amountRequired )
                break;
        }

        return amountSaturated;
    }

    /**
     * Collect all the inventories and return them in a pretty order to allow for inventory flowing (where the system
     * will use the items from each inventory in a flow like order instead of being bound to a single inventory at
     * a time)
     *
     * @return a list of inventories in the order of: Linked, Player, Player inventory slotted inventories (dank null)
     */
    private static List<Pair<InventoryType, IItemHandler>> collectInventories(ItemStack gadget, EntityPlayer player, World world, NetworkIO.Operation operation) {
        List<Pair<InventoryType, IItemHandler>> inventories = new ArrayList<>();

        // Always provide the remote inventory first
        IItemHandler linked = GadgetUtils.getRemoteInventory(gadget, world, player, operation);
        if( linked != null )
            inventories.add(Pair.of(InventoryType.LINKED, linked));

        // Then supply the player inventory if it exists (it should)
        IItemHandler currentInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if( currentInv == null )
            return inventories;

        inventories.add(Pair.of(InventoryType.PLAYER, currentInv));

        // Finally, add all inventory bound inventories to the list. Then return them all.
        for (int i = 0; i < currentInv.getSlots(); ++i) {
            ItemStack itemStack = currentInv.getStackInSlot(i);
            if (!itemStack.hasCapability(ITEM_HANDLER_CAPABILITY, null))
                continue;

            inventories.add(Pair.of(InventoryType.OTHER, itemStack.getCapability(ITEM_HANDLER_CAPABILITY, null)));
        }

        return inventories;
    }

    /**
     * -------------------------------------
     * START WEIRD COUNT ITEM IMPLEMENTATION
     * -------------------------------------
     */
    public interface IRemoteInventoryProvider {
        int countItem(ItemStack tool, ItemStack stack);
    }

    /**
     * Call {@link GadgetUtils#clearCachedRemoteInventory GadgetUtils#clearCachedRemoteInventory} when done using this method
     */
    public static int countItem(ItemStack itemStack, EntityPlayer player, World world) {
        return countItem(itemStack, player, (tool, stack) -> {
            IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(tool, world, player);
            return remoteInventory == null ? 0 : countInContainer(remoteInventory, stack.getItem(), stack.getMetadata());
        });
    }

    public static int countItem(ItemStack itemStack, EntityPlayer player, IRemoteInventoryProvider remoteInventory) {
        if (player.capabilities.isCreativeMode)
            return Integer.MAX_VALUE;

        long count = remoteInventory.countItem(GadgetGeneric.getGadget(player), itemStack);

        IItemHandler currentInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if( currentInv == null )
            return 0;

        List<Integer> slots = findItem(itemStack.getItem(), itemStack.getMetadata(), currentInv);
        List<IItemHandler> invContainers = findInvContainers(player);
        if (slots.size() == 0 && invContainers.size() == 0 && count == 0) {
            return 0;
        }

        if (invContainers.size() > 0) {
            for (IItemHandler container : invContainers) {
                count += countInContainer(container, itemStack.getItem(), itemStack.getMetadata());
            }
        }

        for (int slot : slots) {
            ItemStack stackInSlot = currentInv.getStackInSlot(slot);
            count += stackInSlot.getCount();
        }
        return MathTool.longToInt(count);
    }

    public static IntList countItems(List<ItemStack> items, EntityPlayer player) {
        IntList result = new IntArrayList();
        for (ItemStack item : items) {
            result.add(countItem(item, player, player.world));
        }
        return result;
    }

    public static int countPaste(EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return Integer.MAX_VALUE;
        }

        IItemHandler currentInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if( currentInv == null )
            return 0;

        long count = 0;
        Item item = ModItems.constructionPaste;
        List<Integer> slots = findItem(item, 0, currentInv);
        if (slots.size() > 0) {
            for (int slot : slots) {
                ItemStack stackInSlot = currentInv.getStackInSlot(slot);
                count += stackInSlot.getCount();
            }
        }
        List<Integer> containerSlots = findItemClass(GenericPasteContainer.class, currentInv);
        if (containerSlots.size() > 0) {
            for (int slot : containerSlots) {
                ItemStack stackInSlot = currentInv.getStackInSlot(slot);
                if (stackInSlot.getItem() instanceof GenericPasteContainer) {
                    count += GenericPasteContainer.getPasteAmount(stackInSlot);
                }
            }
        }
        return MathTool.longToInt(count);
    }

    /**
     * -------------------------------------
     * END WEIRD COUNT ITEM IMPLEMENTATION
     * -------------------------------------
     */

    public static ItemStack addPasteToContainer(EntityPlayer player, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ConstructionPaste))
            return itemStack;

        IItemHandler currentInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if( currentInv == null )
            return itemStack;

        List<Integer> slots = findItemClass(GenericPasteContainer.class, currentInv);
        if (slots.size() == 0)
            return itemStack;

        Map<Integer, Integer> slotMap = new HashMap<>();
        for (int slot : slots) {
            slotMap.put(slot, GenericPasteContainer.getPasteAmount(currentInv.getStackInSlot(slot)));
        }
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(slotMap.entrySet());
        Comparator<Map.Entry<Integer, Integer>> comparator = Comparator.comparing(Map.Entry::getValue);
        comparator = comparator.reversed();
        list.sort(comparator);


        for (Map.Entry<Integer, Integer> entry : list) {
            ItemStack containerStack = currentInv.getStackInSlot(entry.getKey());
            int maxAmount = ((GenericPasteContainer) containerStack.getItem()).getMaxCapacity();
            int pasteInContainer = GenericPasteContainer.getPasteAmount(containerStack);
            int freeSpace = maxAmount - pasteInContainer;
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

    public static boolean usePaste(EntityPlayer player, int count) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }

        IItemHandler currentInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if( currentInv == null )
            return false;

        List<Integer> slots = findItem(ModItems.constructionPaste, 0, currentInv);
        if (slots.size() > 0) {
            for (int slot : slots) {
                ItemStack pasteStack = currentInv.getStackInSlot(slot);
                if (pasteStack.getCount() >= count) {
                    pasteStack.shrink(count);
                    return true;
                }
            }
        }

        List<Integer> containerSlots = findItemClass(GenericPasteContainer.class, currentInv);
        if (containerSlots.size() > 0) {
            for (int slot : containerSlots) {
                ItemStack containerStack = currentInv.getStackInSlot(slot);
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

    private static List<IItemHandler> findInvContainers(EntityPlayer player) {
        List<IItemHandler> containers = new ArrayList<>();

        IItemHandler currentInv = player.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if( currentInv == null )
            return containers;

        for (int i = 0; i < currentInv.getSlots(); ++i) {
            ItemStack itemStack = currentInv.getStackInSlot(i);
            if (itemStack.hasCapability(ITEM_HANDLER_CAPABILITY, null))
                containers.add(itemStack.getCapability(ITEM_HANDLER_CAPABILITY, null));
        }

        return containers;
    }

    public static int countInContainer(IItemHandler container, Item item, int meta) {
        int count = 0;
        ItemStack tempItem;
        for (int i = 0; i < container.getSlots(); i++) {
            tempItem = container.getStackInSlot(i);
            if (tempItem.getItem() == item && tempItem.getMetadata() == meta) {
                count += tempItem.getCount();
            }
        }
        return count;
    }

    private static List<Integer> findItem(Item item, int meta, IItemHandler itemHandler) {
        List<Integer> slots = new ArrayList<>();
        if( itemHandler == null )
            return slots;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item && meta == stack.getMetadata())
                slots.add(i);
        }
        return slots;
    }

    private static List<Integer> findItemClass(Class c, IItemHandler itemHandler) {
        List<Integer> slots = new ArrayList<>();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && c.isInstance(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static ItemStack getSilkTouchDrop(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        int i = 0;
        if (item.getHasSubtypes()) {
            i = state.getBlock().damageDropped(state);
        }
        return new ItemStack(item, 1, i);
    }

    public static IBlockState getSpecificStates(IBlockState originalState, World world, EntityPlayer player, BlockPos pos, ItemStack tool) {
        IBlockState placeState;
        Block block = originalState.getBlock();

        ItemStack item;
        try {
            item = block.getPickBlock(originalState, null, world, pos, player);
        } catch (Exception ignored) {
            // This may introduce issues. I hope it doesn't
            item = InventoryManipulation.getSilkTouchDrop(originalState);
        }

        int meta = item.getMetadata();
        try {
            placeState = originalState.getBlock().getStateForPlacement(world, pos, EnumFacing.UP, 0, 0, 0, meta, player, EnumHand.MAIN_HAND);
        } catch (Exception var8) {
            placeState = originalState.getBlock().getDefaultState();
        }
        for (IProperty prop : placeState.getPropertyKeys()) {
            if (tool.getItem() instanceof GadgetCopyPaste) {
                if (SAFE_PROPERTIES_COPY_PASTE.contains(prop)) {
                    placeState = placeState.withProperty(prop, originalState.getValue(prop));
                }
            } else {
                if (SAFE_PROPERTIES.contains(prop)) {
                    placeState = placeState.withProperty(prop, originalState.getValue(prop));
                }
            }
        }
        return placeState;

    }

    /**
     * Find an item stack in either hand that delegates to the given {@code itemClass}.
     * <p>
     * This method will prioritize primary hand, which means if player hold the desired item on both hands, it will
     * choose his primary hand first. If neither hands have the desired item stack, it will return {@link
     * ItemStack#EMPTY}.
     *
     * @return {@link ItemStack#EMPTY} when neither hands met the parameter.
     */
    public static ItemStack getStackInEitherHand(EntityPlayer player, Class<?> itemClass) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (itemClass.isInstance(mainHand.getItem()))
            return mainHand;
        ItemStack offhand = player.getHeldItemOffhand();
        if (itemClass.isInstance(offhand.getItem()))
            return offhand;
        return ItemStack.EMPTY;
    }

    public static String formatItemCount(int maxSize, int count) {
        int stacks = count / maxSize; // Integer division automatically floors
        int leftover = count % maxSize;
        if (stacks == 0)
            return String.valueOf(leftover);
        return stacks + "×" + maxSize + "+" + leftover;
    }
}
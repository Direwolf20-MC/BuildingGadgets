package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.GenericPasteContainer;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

/**
 * @MajorTuvok
 * Please check of the changes with the Sets as I've had to to some guess work
 */
public class InventoryManipulation {
    private static IProperty AXIS = EnumProperty.create("axis", EnumFacing.Axis.class);

    private static final Set<IProperty> SAFE_PROPERTIES =
            ImmutableSet.of(BlockSlab.TYPE, BlockStairs.HALF, BlockLog.AXIS, AXIS, BlockDirectional.FACING, BlockStairs.FACING, BlockTrapDoor.HALF, BlockStairs.SHAPE, BlockLever.POWERED, BlockRedstoneRepeater.DELAY);

    private static final Set<IProperty> SAFE_PROPERTIES_COPY_PASTE =
            ImmutableSet.<IProperty>builder().addAll(SAFE_PROPERTIES).addAll(ImmutableSet.of(BlockRail.SHAPE, BlockRailPowered.SHAPE)).build();

    public static boolean giveItem(ItemStack itemStack, EntityPlayer player, IWorld world) {
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
        InventoryPlayer inv = player.inventory;
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
        ItemStack tool = GadgetGeneric.getGadget(player);
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

    public static boolean useItem(ItemStack itemStack, EntityPlayer player, int count, World world) {
        if (player.isCreative()) {
            return true;
        }

        ItemStack tool = GadgetGeneric.getGadget(player);
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


        InventoryPlayer inv = player.inventory;

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

    public static interface IRemoteInventoryProvider {
        int countItem(ItemStack tool, ItemStack stack);
    }

    public static int countItem(ItemStack itemStack, EntityPlayer player, World world) {
        return countItem(itemStack, player, (tool, stack) -> {
            IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(tool, world);
            return remoteInventory == null ? 0 : countInContainer(remoteInventory, stack.getItem());
        });
    }

    public static int countItem(ItemStack itemStack, EntityPlayer player, IRemoteInventoryProvider remoteInventory) {
        if (player.isCreative())
            return Integer.MAX_VALUE;

        long count = remoteInventory.countItem(GadgetGeneric.getGadget(player), itemStack);
        InventoryPlayer inv = player.inventory;
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

    public static int countPaste(EntityPlayer player) {
        if (player.isCreative())
            return Integer.MAX_VALUE;

        long count = 0;
        InventoryPlayer inv = player.inventory;
        Item item = BGItems.constructionPaste;
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

    public static ItemStack addPasteToContainer(EntityPlayer player, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ConstructionPaste)) {
            return itemStack;
        }
        InventoryPlayer inv = player.inventory;
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
        if (player.isCreative()) {
            return true;
        }
        InventoryPlayer inv = player.inventory;
        List<Integer> slots = findItem(BGItems.constructionPaste, inv);
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

    private static List<IItemHandler> findInvContainers(InventoryPlayer inv) {
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

    private static List<Integer> findItem(Item item, InventoryPlayer inv) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static List<Integer> findItemClass(Class c, InventoryPlayer inv) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && c.isInstance(stack.getItem())) {
                slots.add(i);
            }
        }
        return slots;
    }

    public static ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(state.getBlock());
    }

    public static IBlockState getSpecificStates(IBlockState originalState, World world, EntityPlayer player, BlockPos pos, ItemStack tool) {
        IBlockState placeState;
        Block block = originalState.getBlock();
        ItemStack item = block.getPickBlock(originalState, null, world, pos, player);

        try {
            placeState = originalState.getBlock().getStateForPlacement(
                    new BlockItemUseContext(world, player, item, pos, EnumFacing.UP, 0.5F, 0.0F, 0.5F)
            );
        } catch (Exception var8) {
            placeState = originalState.getBlock().getDefaultState();
        }

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
        return placeState;

    }
}

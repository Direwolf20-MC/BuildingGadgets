package com.direwolf20.buildinggadgets.common.tools;

import it.unimi.dsi.fastutil.doubles.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Sorter
 * <p>
 * The sorter class is made to contain all sorts of different sorting algorithms
 * <p>
 * Please use subclasses when more than one sorting method is used for a specific data type / class. For example our
 * static Blocks class used for sorting any data related to a Minecraft Block
 */
public class Sorter {

    public static class Blocks {

        /**
         * @param list   an unsorted {@link BlockPos} Collection
         * @param player the player
         * @return a sorted by distance {@link List} of {@link BlockPos}
         */
        public static List<BlockPos> byDistance(Collection<BlockPos> list, EntityPlayer player) {
            List<BlockPos> sortedList = new ArrayList<>();

            Double2ObjectMap<BlockPos> rangeMap = new Double2ObjectArrayMap<>(list.size());
            DoubleSortedSet distances = new DoubleRBTreeSet();

            double x = player.posX,
                    y = player.posY + player.getEyeHeight(),
                    z = player.posZ;

            list.forEach(pos -> {
                double distance = pos.distanceSqToCenter(x, y, z);

                rangeMap.put(distance, pos);
                distances.add(distance);
            });

            for (double dist : distances) {
                sortedList.add(rangeMap.get(dist));
            }

            return sortedList;
        }

    }

    public static class ItemStacks {

        private static final Comparator<ItemStack> COMPARATOR_NAME = Comparator.comparing(ItemStack::getDisplayName);
        private static final Comparator<ItemStack> COMPARATOR_NAME_REVERSED = COMPARATOR_NAME.reversed();
        private static final Comparator<ItemStack> COMPARATOR_COUNT = Comparator.comparing(ItemStack::getCount);
        private static final Comparator<ItemStack> COMPARATOR_COUNT_REVERSED = COMPARATOR_COUNT.reversed();

        /**
         * Sort the given list consisting {@link ItemStack} alphabetically, from <i>a</i> to <i>z</i>.
         * <p>
         * This method will copy all content of the original list to another {@link ArrayList}, and sort them using
         * {@link List#sort(Comparator)} by comparing the {@link ItemStack#getDisplayName()}.
         *
         * @param stacks the list of {@link ItemStack} to be sorted
         * @return A copy of the original list that is sorted.
         */
        public static List<ItemStack> byName(List<ItemStack> stacks, boolean reverse) {
            List<ItemStack> sorted = new ArrayList<>(stacks);
            byNameInplace(sorted, reverse);
            return sorted;
        }

        /**
         * Basically {@link #byName(List, boolean)} except it sorts them in place instead of creating a new list.
         *
         * @return the input {@link List}
         * @see #byName(List, boolean)
         */
        public static List<ItemStack> byNameInplace(List<ItemStack> stacks, boolean reverse) {
            stacks.sort(reverse ? COMPARATOR_NAME_REVERSED : COMPARATOR_NAME);
            return stacks;
        }

        /**
         * Sort the given list consisting {@link ItemStack} by stack size.
         * <p>
         * This method will copy all content of the original list to another {@link ArrayList}, and sort them using
         * {@link List#sort(Comparator)} by comparing the {@link ItemStack#getCount()}.
         *
         * @param stacks the list of {@link ItemStack} to be sorted
         * @return A copy of the original list that is sorted.
         */
        public static List<ItemStack> byCount(List<ItemStack> stacks, boolean reverse) {
            List<ItemStack> sorted = new ArrayList<>(stacks);
            byCount(sorted, reverse);
            return sorted;
        }

        /**
         * Basically {@link #byCount(List, boolean)} except it sorts them in place instead of creating a new list.
         *
         * @return the input {@link List}
         * @see #byCount(List, boolean)
         */
        public static List<ItemStack> byCountInplace(List<ItemStack> stacks, boolean reverse) {
            stacks.sort(reverse ? COMPARATOR_COUNT_REVERSED : COMPARATOR_COUNT);
            return stacks;
        }

    }

}

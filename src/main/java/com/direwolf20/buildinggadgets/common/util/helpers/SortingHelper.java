package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * SortingHelper
 *
 * The sorter class is made to contain all sorts of different sorting algorithms
 *
 * Please use subclasses when more than one sorting method is used for a
 * specific data type / class. For example our static Blocks class used
 * for sorting any data related to a Minecraft Block
 */
public class SortingHelper {

    public static class Blocks {

        public static List<BlockPos> byDistance(Iterable<BlockPos> list, PlayerEntity player) {
            return byDistance(list, Function.identity(), player);
        }

        /**
         *
         * @param list an unsorted {@link BlockPos} Collection
         * @param player the player
         * @return a sorted by distance {@link List} of {@link BlockPos}
         */
        public static <T> List<T> byDistance(Iterable<T> list, Function<T, ? extends BlockPos> posExtractor, PlayerEntity player) {
            List<T> sortedList = new ArrayList<>();

            Double2ObjectMap<T> rangeMap = new Double2ObjectArrayMap<>();
            DoubleSortedSet distances = new DoubleRBTreeSet();

            double  x = player.posX,
                    y = player.posY + player.getEyeHeight(),
                    z = player.posZ;

            list.forEach(obj -> {
                BlockPos pos = posExtractor.apply(obj);
                double distance = pos.distanceSq(x, y, z, true);

                rangeMap.put(distance, obj);
                distances.add(distance);
            } );

            for (double dist : distances) {
                sortedList.add(rangeMap.get(dist));
            }

            return sortedList;
        }

    }

    public static class RenderSorter {
        private final List<PlacementTarget> orderedTargets;
        private final IBuildContext context;
        private List<PlacementTarget> sortedTargets;

        public RenderSorter(IBuildContext context, int estimatedSize) {
            this.orderedTargets = new ArrayList<>(estimatedSize);
            this.context = Objects.requireNonNull(context);
            assert context.getBuildingPlayer() != null;
            this.sortedTargets = null;
        }

        public void onPlaced(PlacementTarget target) {
            this.orderedTargets.add(target);
        }

        public List<PlacementTarget> getOrderedTargets() {
            return Collections.unmodifiableList(orderedTargets);
        }

        public List<PlacementTarget> getSortedTargets() {
            if (sortedTargets == null)
                sortedTargets = sort(orderedTargets);
            return Collections.unmodifiableList(sortedTargets);

        }

        protected List<PlacementTarget> sort(List<PlacementTarget> orderedTargets) {
            return Blocks.byDistance(orderedTargets, PlacementTarget::getPos, getPlayer());
        }

        public IBuildContext getContext() {
            return context;
        }

        public PlayerEntity getPlayer() {
            assert context.getBuildingPlayer() != null;
            return context.getBuildingPlayer();
        }
    }


}

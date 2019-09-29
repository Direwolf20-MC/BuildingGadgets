package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    public static class Blocks { //TODO move sort to RenderSorter#sort when everything uses PlacementTargets and delete this afterwards...

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
            double  x = player.posX,
                    y = player.posY + player.getEyeHeight(),
                    z = player.posZ;
            return StreamSupport.stream(list.spliterator(), false)
                    .map(t -> new TargetObject<T>(posExtractor.apply(t).distanceSq(x, y, z, true), t))
                    .sorted(TargetObject.BY_DISTANCE)
                    .map(TargetObject::getTarget)
                    //A linked list is the optimal Datastructure here - we don't need random access. Notice that changing that to an ArrayList will add an Factor of 1.2!
                    .collect(Collectors.toCollection(LinkedList::new));
        }

    }

    public static class RenderSorter {
        private final List<PlacementTarget> orderedTargets;
        private final PlayerEntity player;
        private List<PlacementTarget> sortedTargets;

        public RenderSorter(PlayerEntity player, int estimatedSize) {
            this.orderedTargets = new ArrayList<>(estimatedSize);
            this.player = player;
            this.sortedTargets = null;
        }

        public void onPlaced(PlacementTarget target) {
            this.orderedTargets.add(target);
        }

        public List<PlacementTarget> getOrderedTargets() {
            return Collections.unmodifiableList(orderedTargets);
        }

        public List<PlacementTarget> getSortedTargets() {
            if (sortedTargets == null) {
                //long nanoTime = System.nanoTime();
                sortedTargets = sort(orderedTargets);
                //long dif = System.nanoTime() - nanoTime;
                //BuildingGadgets.LOG.info("Render sorting took {} nano seconds.", dif);
            }
            return Collections.unmodifiableList(sortedTargets);

        }

        private List<PlacementTarget> sort(List<PlacementTarget> orderedTargets) {
            return Blocks.byDistance(orderedTargets, PlacementTarget::getPos, getPlayer());
        }

        public PlayerEntity getPlayer() {
            return player;
        }
    }

    private static final class TargetObject<T> {
        public static final Comparator<TargetObject<?>> BY_DISTANCE = Comparator.<TargetObject<?>>comparingDouble(TargetObject::getDist).reversed();
        private final double dist;
        private final T target;

        public TargetObject(double dist, T target) {
            this.dist = dist;
            this.target = target;
        }

        public T getTarget() {
            return target;
        }

        public double getDist() {
            return dist;
        }
    }


}

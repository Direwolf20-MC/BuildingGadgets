package com.direwolf20.buildinggadgets.common.util.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

            return byDistance(StreamSupport.stream(list.spliterator(), false), posExtractor, player)
                    //A linked list is the optimal Datastructure here - we don't need random access. Notice that changing that to an ArrayList will add an Factor of 1.2!
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        public static <T> Stream<T> byDistance(Stream<T> stream, Function<T, ? extends BlockPos> posExtractor, PlayerEntity player) {
            Vec3d pos = player.getPositionVec().add(0, player.getEyeHeight(), 0);

            return stream
                    .map(t -> new TargetObject<T>(posExtractor.apply(t).distanceSq(pos.x, pos.y, pos.z, true), t))
                    .sorted(TargetObject.BY_DISTANCE)
                    .map(TargetObject::getTarget);
        }

    }


    private static final class TargetObject<T> {
        public static final Comparator<TargetObject<?>> BY_DISTANCE = Comparator.<TargetObject<?>>comparingDouble(TargetObject::getDist);
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

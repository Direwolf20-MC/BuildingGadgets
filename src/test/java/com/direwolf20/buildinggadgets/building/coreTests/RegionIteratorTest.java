package com.direwolf20.buildinggadgets.building.coreTests;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class RegionIteratorTest {

    @Test
    void iteratorShouldHaveSameNumberOfElementsAsSizeMethod() {
        Region region = new Region(-3, -3, -3, 3, 3, 3);
        assertEquals(region.size(), ImmutableList.copyOf(region.iterator()).size());
    }

    @Test
    void iteratorShouldHave1PositionWhenTwoVertexesAreTheSame() {
        Region region = new Region(1, 1, 1, 1, 1, 1);
        Iterator<BlockPos> it = region.iterator();
        assertEquals(new BlockPos(1, 1, 1), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void iteratorShouldGoInAccentingOrderWhereZFirstYSecondXThirdAllPositiveCaseHardcoded() {
        Region region = new Region(1, 1, 1, 2, 2, 2);
        Iterator<BlockPos> it = region.iterator();

        assertEquals(new BlockPos(1, 1, 1), it.next());
        assertEquals(new BlockPos(1, 1, 2), it.next());
        assertEquals(new BlockPos(1, 2, 1), it.next());
        assertEquals(new BlockPos(1, 2, 2), it.next());
        assertEquals(new BlockPos(2, 1, 1), it.next());
        assertEquals(new BlockPos(2, 1, 2), it.next());
        assertEquals(new BlockPos(2, 2, 1), it.next());
        assertEquals(new BlockPos(2, 2, 2), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void iteratorShouldGoInAccentingOrderWhereZFirstYSecondXThirdAllNegativeCaseHardcoded() {
        Region region = new Region(-2, -2, -2, -1, -1, -1);
        Iterator<BlockPos> it = region.iterator();

        assertEquals(new BlockPos(-2, -2, -2), it.next());
        assertEquals(new BlockPos(-2, -2, -1), it.next());
        assertEquals(new BlockPos(-2, -1, -2), it.next());
        assertEquals(new BlockPos(-2, -1, -1), it.next());
        assertEquals(new BlockPos(-1, -2, -2), it.next());
        assertEquals(new BlockPos(-1, -2, -1), it.next());
        assertEquals(new BlockPos(-1, -1, -2), it.next());
        assertEquals(new BlockPos(-1, -1, -1), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void iteratorShouldBehaveSimilarlyInNegativeRegionAsPositiveRegionWhereStartsInMinEndsInMax() {
        Region region = new Region(0, 0, 0, 0, -8, 0);
        Iterator<BlockPos> it = region.iterator();

        assertEquals(new BlockPos(0, -8, 0), it.next());
        assertEquals(new BlockPos(0, -7, 0), it.next());
        assertEquals(new BlockPos(0, -6, 0), it.next());
        assertEquals(new BlockPos(0, -5, 0), it.next());
        assertEquals(new BlockPos(0, -4, 0), it.next());
        assertEquals(new BlockPos(0, -3, 0), it.next());
        assertEquals(new BlockPos(0, -2, 0), it.next());
        assertEquals(new BlockPos(0, -1, 0), it.next());
        assertEquals(new BlockPos(0, 0, 0), it.next());
        assertFalse(it.hasNext());
    }

    private void iteratorYieldingOrderSortedComparisionBase(Region region) {
        ImmutableList<BlockPos> sorted = ImmutableList.sortedCopyOf(GadgetUtils.POSITION_COMPARATOR, region);
        ImmutableList<BlockPos> actual = ImmutableList.copyOf(region);

        assertEquals(sorted, actual);
    }

    @Test
    void iteratorShouldGoInAccentingOrderWhereZFirstYSecondXThirdAllPositiveCaseSortedComparision() {
        Region region = new Region(-2, -2, -2, -1, -1, -1);
        iteratorYieldingOrderSortedComparisionBase(region);
    }

    @Test
    void iteratorShouldGoInAccentingOrderWhereZFirstYSecondXThirdAllNegativeCaseSortedComparision() {
        Region region = new Region(1, 1, 1, 2, 2, 2);
        iteratorYieldingOrderSortedComparisionBase(region);
    }

    private final Random random = new Random();
    /**
     * Range -maxCoordinate to maxCoordinate
     */
    private final int maxCoordinate = 16;
    /**
     * Distribute from -maxCoordinate to maxCoordinate. +1 for the exclusive upper bound
     */
    private final int upperBound = maxCoordinate * 2 + 1;

    @RepeatedTest(4)
    void iteratorShouldGoInAccentingOrderWhereZFirstYSecondXThirdRandomSortedComparision() {
        int minX = random.nextInt(upperBound) - maxCoordinate;
        int minY = random.nextInt(upperBound) - maxCoordinate;
        int minZ = random.nextInt(upperBound) - maxCoordinate;
        int maxX = random.nextInt(upperBound) - maxCoordinate;
        int maxY = random.nextInt(upperBound) - maxCoordinate;
        int maxZ = random.nextInt(upperBound) - maxCoordinate;
        iteratorYieldingOrderSortedComparisionBase(new Region(minX, minY, minZ, maxX, maxY, maxZ));
    }

}

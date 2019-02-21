package com.direwolf20.buildinggadgets.common.building.region;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class RegionIteratorTest {

    @Test
    public void iteratorShouldHaveSameNumberOfElementsAsSizeMethod() {
        Region region = new Region(1, 1, 1, 3, 3, 3);
        assertEquals(region.size(), ImmutableList.copyOf(region.iterator()).size());
    }

    @Test
    public void iteratorShouldStartGoInAccentingOrderWhereZFirstYSecondXThird() {
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
    }

    @Test
    public void iteratorShouldStartGoInAccentingOrderWhereZFirstYSecondXThirdAllNegativeCase() {
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
    }

}

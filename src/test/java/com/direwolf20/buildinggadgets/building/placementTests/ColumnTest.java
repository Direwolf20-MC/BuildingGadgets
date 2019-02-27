package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.placement.Column;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ColumnTest {

    @Test
    void columnFacingUpShouldReturnSequenceWithAccentingY() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Column> constructor = Column.class.getDeclaredConstructor(BlockPos.class, BlockPos.class);
        constructor.setAccessible(true);

        Column column = constructor.newInstance(BlockPos.ORIGIN, BlockPos.ORIGIN.up(4));
        Iterator<BlockPos> it = column.iterator();

        assertEquals(new BlockPos(0, 0, 0), it.next());
        assertEquals(new BlockPos(0, 1, 0), it.next());
        assertEquals(new BlockPos(0, 2, 0), it.next());
        assertEquals(new BlockPos(0, 3, 0), it.next());
        assertEquals(new BlockPos(0, 4, 0), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void columnCreatedWithFactoryMethodExtendFromShouldOffsetBaseBy1ToGivenFacing() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            Column column = Column.extendFrom(BlockPos.ORIGIN, facing, 15);
            Iterator<BlockPos> it = column.iterator();

            if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                for (int i = 15; i >= 1; i--) {
                    assertEquals(BlockPos.ORIGIN.offset(facing, i), it.next());
                }
            } else {
                for (int i = 1; i <= 15; i++) {
                    assertEquals(BlockPos.ORIGIN.offset(facing, i), it.next());
                }
            }

            assertFalse(it.hasNext());
        }
    }

    @Test
    void columnOnXAxisCenteredAtOriginShouldHaveAccentingX() {
        Column column = Column.centerAt(BlockPos.ORIGIN, EnumFacing.Axis.X, 5);
        Iterator<BlockPos> it = column.iterator();

        assertEquals(new BlockPos(-2, 0, 0), it.next());
        assertEquals(new BlockPos(-1, 0, 0), it.next());
        assertEquals(new BlockPos(0, 0, 0), it.next());
        assertEquals(new BlockPos(1, 0, 0), it.next());
        assertEquals(new BlockPos(2, 0, 0), it.next());
        assertFalse(it.hasNext());
    }

    @RepeatedTest(4)
    void centerAtShouldCeilDownToNearestOddNumberAsSizeRandomParameterSize() {
        int size = MathHelper.clamp(random.nextInt(8), 1, Integer.MAX_VALUE) * 2;
        Column column = Column.centerAt(BlockPos.ORIGIN, EnumFacing.Axis.Y, size);
        Iterator<BlockPos> it = column.iterator();

        for (int i = 0; i < size - 1; i++) {
            it.next();
        }
        assertFalse(it.hasNext());
    }

    private final Random random = new Random();

    @RepeatedTest(4)
    void inclusiveAxisChasingShouldStopBeforeEntityPositionRandomSideRandomDistance() {
        EnumFacing side = EnumFacing.random(random);
        int distance = 0;
        Column column = Column.inclusiveAxisChasing(BlockPos.ORIGIN.offset(side, distance), BlockPos.ORIGIN, side);
        Iterator<BlockPos> it = column.iterator();

        if (side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
            for (int i = distance; i >= 0; i--) {
                assertEquals(BlockPos.ORIGIN.offset(side, i), it.next());
            }
        } else {
            for (int i = 0; i <= distance; i++) {
                assertEquals(BlockPos.ORIGIN.offset(side, i), it.next());
            }
        }
        assertFalse(it.hasNext());
    }

    @Test
    void inclusiveAxisChasingShouldIgnoreNonParametrizedAxisValuesHardcoded() {
        Column column = Column.inclusiveAxisChasing(new BlockPos(8, 4, -9), BlockPos.ORIGIN, EnumFacing.UP);
        Iterator<BlockPos> it = column.iterator();

        //Note that this position will be invalid for the most cases, since this block, the source block, will be a non-air
        assertEquals(new BlockPos(0, 0, 0), it.next());
        assertEquals(new BlockPos(0, 1, 0), it.next());
        assertEquals(new BlockPos(0, 2, 0), it.next());
        assertEquals(new BlockPos(0, 3, 0), it.next());
        assertEquals(new BlockPos(0, 4, 0), it.next());
        assertFalse(it.hasNext());
    }

}

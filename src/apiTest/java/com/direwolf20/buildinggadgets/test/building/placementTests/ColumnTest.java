package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.placement.Column;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        for (EnumFacing facing : EnumFacing.values()) {
            Column column = Column.extendFrom(BlockPos.ORIGIN, facing, 15);
            Iterator<BlockPos> it = column.iterator();

            if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                for (int i = 14; i >= 0; i--) {
                    assertEquals(BlockPos.ORIGIN.offset(facing, i), it.next());
                }
            } else {
                for (int i = 0; i <= 14; i++) {
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

}

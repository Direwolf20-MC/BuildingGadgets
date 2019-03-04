package com.direwolf20.buildinggadgets.test.api.abstraction;

import com.direwolf20.buildinggadgets.api.abstraction.ImmutablePos3s;
import com.direwolf20.buildinggadgets.api.abstraction.Pos3s;
import com.direwolf20.buildinggadgets.test.util.ShortRandom;
import com.direwolf20.buildinggadgets.test.util.annotations.LargeTest;
import com.direwolf20.buildinggadgets.test.util.annotations.MediumTest;
import com.direwolf20.buildinggadgets.test.util.annotations.SingleTest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {
    private static final short RANDOM_BOUND = (short) 1000;
    private static ShortRandom random;

    @BeforeAll
    public static void initRandom() {
        random = new ShortRandom();
    }

    private short randomShort() {
        return random.nextShortIncludingNegative(RANDOM_BOUND);
    }

    private short randomShortNot0() {
        short res = randomShort();
        if (res == 0) return randomShortNot0();
        return res;
    }

    protected Pos3s create(int x, int y, int z) {
        return new Pos3s(x, y, z);
    }

    protected Pos3s create(Pos3s pos) {
        return new Pos3s(pos);
    }

    protected Pos3s create(short x, short y, short z) {
        return new Pos3s(x, y, z);
    }

    private Pos3s generatePos() {
        short x = randomShort();
        short y = randomShort();
        short z = randomShort();
        Pos3s pos = create(x, y, z);
        assertEquals(pos.getX(), x, () -> pos + " was expected to have " + x + " as x-Coord but actually has " + pos.getX());
        assertEquals(pos.getY(), y, () -> pos + " was expected to have " + y + " as y-Coord but actually has " + pos.getY());
        assertEquals(pos.getZ(), z, () -> pos + " was expected to have " + z + " as z-Coord but actually has " + pos.getZ());
        return pos;
    }

    private void posEquals(Pos3s pos, Pos3s other) {
        assertEquals(pos, other, () -> pos + " was expected to equal " + other);
    }

    private void posEqualsBlockPos(Pos3s pos, BlockPos other) {
        assertEquals(pos.toBlockPos(), other, () -> pos + " was expected to equal BlockPos " + other);
    }

    protected Pos3s executeVerifyInstance(Pos3s pos, UnaryOperator<Pos3s> operator) {
        Pos3s res = operator.apply(pos);
        assertSame(res, pos);
        return res;
    }

    @SingleTest
    void positionCopiesCorrectly() {
        Pos3s pos = generatePos();
        Pos3s copy = create(pos);
        assertEquals(pos, copy);
    }

    @LargeTest
    void positionObeysEqualsHashCodeAndComparableContractCaseDifferentXPos() {
        Pos3s pos = generatePos();
        Pos3s pos1 = create(pos.getX() + randomShortNot0(), pos.getY() + randomShortNot0(), pos.getZ() + randomShortNot0());
        assertNotEquals(pos, pos1, () -> pos + " may not be equal to " + pos1);
        int comparePos = pos.compareTo(pos1);
        assertNotEquals(comparePos, 0, () -> pos + " comparison may not be equal to " + pos1);
        int compare = Short.compare(pos.getX(), pos1.getX());
        assertEquals(comparePos, compare, () -> pos + " should have compared x coord as comparison result with " + pos1);
    }

    @LargeTest
    void positionObeysEqualsHashCodeAndComparableContractCaseDifferentYPos() {
        Pos3s pos = generatePos();
        Pos3s pos1 = create(pos.getX(), pos.getY() + randomShortNot0(), pos.getZ() + randomShortNot0());
        assertNotEquals(pos, pos1, () -> pos + " may not be equal to " + pos1);
        int comparePos = pos.compareTo(pos1);
        assertNotEquals(comparePos, 0, () -> pos + " comparison may not be equal to " + pos1);
        int compare = Short.compare(pos.getY(), pos1.getY());
        assertEquals(comparePos, compare, () -> pos + " should have compared y coord as comparison result with " + pos1);
    }

    @LargeTest
    void positionObeysEqualsHashCodeAndComparableContractCaseDifferentZPos() {
        Pos3s pos = generatePos();
        Pos3s pos1 = create(pos.getX(), pos.getY(), pos.getZ() + randomShortNot0());
        assertNotEquals(pos, pos1, () -> pos + " may not be equal to " + pos1);
        int comparePos = pos.compareTo(pos1);
        assertNotEquals(comparePos, 0, () -> pos + " comparison may not be equal to " + pos1);
        int compare = Short.compare(pos.getZ(), pos1.getZ());
        assertEquals(comparePos, compare, () -> pos + " should have compared z coord as comparison result with " + pos1);
    }

    @LargeTest
    void positionObeysEqualsHashCodeAndComparableContractCaseSamePos() {
        Pos3s pos = generatePos();
        Pos3s pos1 = create(pos);
        assertEquals(pos, pos1, () -> pos + " has to be equal to " + pos1);
        int comparePos = pos.compareTo(pos1);
        assertEquals(comparePos, 0, () -> pos + " should compare to 0 with " + pos1);
        assertEquals(pos.hashCode(), pos1.hashCode(), () -> "Hashcodes of identical Positions " + pos + " and " + pos1 + " should be equal");
    }

    @SingleTest
    void positionAddShouldWorkAsBlockPosAdd() {
        Pos3s pos3s = generatePos();
        BlockPos blockPos = pos3s.toBlockPos();
        short xAdd = randomShort();
        short yAdd = randomShort();
        short zAdd = randomShort();
        pos3s = executeVerifyInstance(pos3s, p -> p.add(xAdd, yAdd, zAdd));
        blockPos = blockPos.add(xAdd, yAdd, zAdd);
        posEqualsBlockPos(pos3s, blockPos);
    }

    @SingleTest
    void positionSubtractShouldWorkAsBlockPosSubtract() {
        Pos3s pos3s = generatePos();
        BlockPos blockPos = pos3s.toBlockPos();
        short xSub = randomShort();
        short ySub = randomShort();
        short zSub = randomShort();
        pos3s = executeVerifyInstance(pos3s, p -> p.subtract(xSub, ySub, zSub));
        blockPos = blockPos.subtract(new BlockPos(xSub, ySub, zSub));
        posEqualsBlockPos(pos3s, blockPos);
    }

    @SingleTest
    void positionSetToShouldSetToOtherPos() {
        Pos3s pos1 = generatePos();
        Pos3s pos2;
        do {
            pos2 = generatePos();
        } while (pos2.equals(pos1));
        final Pos3s pos = pos2;
        Pos3s set = executeVerifyInstance(pos1, p -> p.setTo(pos));
        posEquals(set, pos);
    }

    private void verifyOffsetsCorrectly(EnumFacing facing, Pos3s pos, short amount) {
        Pos3s copy = executeVerifyInstance(create(pos), p -> p.offset(facing, amount));
        Pos3s manualOffset = create(
                pos.getX() + facing.getXOffset() * amount,
                pos.getY() + facing.getYOffset() * amount,
                pos.getZ() + facing.getZOffset() * amount);
        assertEquals(manualOffset, copy, () -> pos + " was expected to offset to " + manualOffset + " given " + facing + " instead of " + copy);
    }

    @MediumTest
    void positionOffsetsCorrectly() {
        Pos3s pos = generatePos();
        EnumFacing[] facings = EnumFacing.values();
        short amount = randomShort();
        for (EnumFacing facing : facings) {
            verifyOffsetsCorrectly(facing, pos, amount);
        }
    }

    @SingleTest
    void positionRotatesCorrectlyX() {
        Pos3s pos = create(0, 1, 1);
        Pos3s clock90 = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.X));
        Pos3s counterClock90 = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.COUNTERCLOCKWISE_90, EnumFacing.Axis.X));
        Pos3s mirror = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.CLOCKWISE_180, EnumFacing.Axis.X));
        posEquals(clock90, create(0, 1, - 1));
        posEquals(counterClock90, create(0, - 1, 1));
        posEquals(mirror, create(0, - 1, - 1));
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.X));
        posEquals(mirror, clock90);
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.X));
        posEquals(counterClock90, clock90);
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.X));
        posEquals(pos, clock90);
    }

    @SingleTest
    void positionRotatesCorrectlyY() {
        Pos3s pos = create(1, 0, 1);
        Pos3s clock90 = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Y));
        Pos3s counterClock90 = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.COUNTERCLOCKWISE_90, EnumFacing.Axis.Y));
        Pos3s mirror = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.CLOCKWISE_180, EnumFacing.Axis.Y));
        posEquals(clock90, create(- 1, 0, 1));
        posEquals(counterClock90, create(1, 0, - 1));
        posEquals(mirror, create(- 1, 0, - 1));
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Y));
        posEquals(mirror, clock90);
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Y));
        posEquals(counterClock90, clock90);
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Y));
        posEquals(pos, clock90);
    }

    @SingleTest
    void positionRotatesCorrectlyZ() {
        Pos3s pos = create(1, 1, 0);
        Pos3s clock90 = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Z));
        Pos3s counterClock90 = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.COUNTERCLOCKWISE_90, EnumFacing.Axis.Z));
        Pos3s mirror = executeVerifyInstance(create(pos), p -> p.rotate(Rotation.CLOCKWISE_180, EnumFacing.Axis.Z));
        posEquals(clock90, create(1, - 1, 0));
        posEquals(counterClock90, create(- 1, 1, 0));
        posEquals(mirror, create(- 1, - 1, 0));
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Z));
        posEquals(mirror, clock90);
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Z));
        posEquals(counterClock90, clock90);
        clock90 = executeVerifyInstance(clock90, p -> p.rotate(Rotation.CLOCKWISE_90, EnumFacing.Axis.Z));
        posEquals(pos, clock90);
    }

    @MediumTest
    void distanceReturnsEuclideanNorm() {
        Pos3s pos = generatePos();
        double dis = pos.getDistance(ImmutablePos3s.ORIGIN);
        assertEquals(dis, Math.sqrt(pos.getX() * pos.getX() + pos.getY() * pos.getY() + pos.getZ() * pos.getZ()));
    }

    @MediumTest
    void distanceReturnsEuclideanNormOfDiffVec() {
        Pos3s pos = generatePos();
        Pos3s other = generatePos();
        Pos3s diff = create(pos).subtract(other);
        double dis = pos.getDistance(other);
        double difDis = diff.getDistance(ImmutablePos3s.ORIGIN);
        assertEquals(dis, difDis);
    }

    @SingleTest
    void throwsOnNull() {
        Pos3s pos = generatePos();
        assertThrows(NullPointerException.class, () -> pos.rotate(null, null));
        assertThrows(NullPointerException.class, () -> pos.rotate(Rotation.NONE, null));
        assertThrows(NullPointerException.class, () -> pos.rotate(null, EnumFacing.Axis.X));
    }
}

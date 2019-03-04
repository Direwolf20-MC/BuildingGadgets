package com.direwolf20.buildinggadgets.test.api.abstraction;

import com.direwolf20.buildinggadgets.api.abstraction.ImmutablePos3s;
import com.direwolf20.buildinggadgets.api.abstraction.Pos3s;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class ImmutablePositionTest extends PositionTest {
    @Override
    protected Pos3s create(int x, int y, int z) {
        return new ImmutablePos3s(x, y, z);
    }

    @Override
    protected Pos3s create(Pos3s pos) {
        return new ImmutablePos3s(pos);
    }

    @Override
    protected Pos3s create(short x, short y, short z) {
        return new ImmutablePos3s(x, y, z);
    }

    @Override
    protected Pos3s executeVerifyInstance(Pos3s pos, UnaryOperator<Pos3s> operator) {
        Pos3s copy = create(pos);
        Pos3s res = operator.apply(pos);
        assertEquals(copy, pos); //immutable instance should not have changed
        assertNotSame(res, pos);
        return res;
    }
}

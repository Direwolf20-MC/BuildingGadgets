package com.direwolf20.buildinggadgets.common.building.blockprovider;

import com.direwolf20.buildinggadgets.common.building.placement.IBlockProvider;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class OriginWrapperTest {

    @Test
    public void wrapperShouldTranslateParameterByAddingMethodGetTranslation() {
        BlockPos[] request = new BlockPos[1];
        request[0] = BlockPos.ORIGIN;

        IBlockProvider handle = pos -> {
            request[0] = pos;
            return null;
        };

        BlockPos translation = new BlockPos(8, 8, 8);
        BlockPos access = new BlockPos(16, 16, 16);
        BlockPos expected = new BlockPos(24, 24, 24);

        handle.translate(translation).at(access);

        assertEquals(expected, request[0]);
    }

}

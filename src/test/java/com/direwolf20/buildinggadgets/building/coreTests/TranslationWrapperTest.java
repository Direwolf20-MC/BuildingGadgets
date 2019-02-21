package com.direwolf20.buildinggadgets.building.coreTests;

import com.direwolf20.buildinggadgets.building.IBlockProvider;
import com.direwolf20.buildinggadgets.building.placement.SingleTypeProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslationWrapperTest {

    @Test
    public void wrapperShouldTranslateParameterByAddingMethodGetTranslation() {
        BlockPos[] request = new BlockPos[1];
        request[0] = BlockPos.ORIGIN;

        IBlockProvider handle = new SingleTypeProvider(null) {
            @Override
            public IBlockState at(BlockPos pos) {
                request[0] = pos;
                return super.at(pos);
            }
        };

        BlockPos translation = new BlockPos(8, 8, 8);
        BlockPos access = new BlockPos(16, 16, 16);
        BlockPos expected = new BlockPos(24, 24, 24);

        handle.translate(translation).at(access);

        assertEquals(expected, request[0]);
    }

}

package com.direwolf20.buildinggadgets.building.coreTests;

import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TranslationWrapperTest {

    private final Random random = new Random();

    private void wrapperShouldTranslateParameterByAddingMethodGetTranslation(BlockPos translation, BlockPos access, BlockPos expected) {
        BlockPos[] request = new BlockPos[1];
        request[0] = BlockPos.ORIGIN;

        IBlockProvider handle = new SingleTypeProvider(null) {
            @Override
            public IBlockState at(BlockPos pos) {
                request[0] = pos;
                return super.at(pos);
            }
        };

        handle.translate(translation).at(access);

        assertEquals(expected, request[0]);
    }

    private void wrapperShouldAccumulateAllTranslations(BlockPos... translations) {
        IBlockProvider wrapper = new SingleTypeProvider(null);
        BlockPos totalTranslation = BlockPos.ORIGIN;
        for (BlockPos translation : translations) {
            wrapper = wrapper.translate(translation);
            totalTranslation = totalTranslation.add(translation);
        }

        assertEquals(totalTranslation, wrapper.getTranslation());
    }

    @Test
    void wrapperShouldTranslateParameterByAddingMethodGetTranslationPositiveCase() {
        BlockPos translation = new BlockPos(8, 8, 8);
        BlockPos access = new BlockPos(16, 16, 16);
        BlockPos expected = new BlockPos(24, 24, 24);
        wrapperShouldTranslateParameterByAddingMethodGetTranslation(translation, access, expected);
    }

    @Test
    void wrapperShouldTranslateParameterByAddingMethodGetTranslationNegativeCase() {
        BlockPos translation = new BlockPos(-8, -8, -8);
        BlockPos access = new BlockPos(-16, -16, -16);
        BlockPos expected = new BlockPos(-24, -24, -24);
        wrapperShouldTranslateParameterByAddingMethodGetTranslation(translation, access, expected);
    }

    @Test
    void wrapperShouldTranslateParameterByAddingMethodGetTranslationMixedCase() {
        BlockPos translation = new BlockPos(-2, -2, -2);
        BlockPos access = new BlockPos(18, 18, 18);
        BlockPos expected = new BlockPos(16, 16, 16);
        wrapperShouldTranslateParameterByAddingMethodGetTranslation(translation, access, expected);
    }

    @Test
    void wrapperShouldAccumulateAllTranslationsCaseRandomMixedRandom() {
        BlockPos[] translations = new BlockPos[4];
        for (int i = 0; i < 4; i++) {
            int x = random.nextInt(65) - 32;
            int y = random.nextInt(65) - 32;
            int z = random.nextInt(65) - 32;
            translations[i] = new BlockPos(x, y, z);
        }
        wrapperShouldAccumulateAllTranslations(translations);
    }

}

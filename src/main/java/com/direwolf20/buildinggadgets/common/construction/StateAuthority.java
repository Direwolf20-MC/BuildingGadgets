package com.direwolf20.buildinggadgets.common.construction;

import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.minecraft.state.properties.BlockStateProperties.*;

/**
 * This class should handle anything to do with state modification and allowed states. It is
 * literally the Authority of how the mod handles states :D
 *
 * @todo: Add IMC
 */
public class StateAuthority {
    public static final List<IProperty<?>> BANNED_STATES = Arrays.asList(
            AGE_0_1, AGE_0_2, AGE_0_3, AGE_0_5, AGE_0_7, AGE_0_15, AGE_0_25,
            OPEN, POWERED, WATERLOGGED, SNOWY, TRIGGERED, BITES_0_6, DISTANCE_1_7 , EGGS_1_4 , HATCH_0_2, LAYERS_1_8,
            LEVEL_0_3, LEVEL_0_8, LEVEL_1_8, HONEY_LEVEL, LEVEL_0_15, MOISTURE_0_7, NOTE_0_24, PICKLES_1_4, POWER_0_15,
            STAGE_0_1, DISTANCE_0_7, CHEST_TYPE, BED_PART, NOTE_BLOCK_INSTRUMENT, STRUCTURE_BLOCK_MODE, BAMBOO_LEAVES,
            EXTENDED, LOCKED
    );

    /**
     * Handles the cleaning of states to ensure we're not copying over potentially
     * abusive states like growth of blocks
     */
    public static BlockState pipe(BlockState state) {
        BlockState subjectState = state.getBlock().getDefaultState();

        // Remove properties from the state
        Collection<IProperty<?>> properties = state.getProperties();
        for (IProperty<?> property : properties) {
            // Skip banned states
            if (BANNED_STATES.contains(property)) {
                continue;
            }

            subjectState = applyProperty(subjectState, state, property);
        }

        // Literally just for leaves when being placed by our mod
        if (state.getProperties().contains(PERSISTENT))
            subjectState = subjectState.with(PERSISTENT, true);

        return subjectState;
    }

    /**
     * Required due to reflection. Mutates a states properties by getting the original state value
     * from the original state and applying it to the subject state.
     */
    private static <T extends Comparable<T>> BlockState applyProperty(
            BlockState subjectState,
            BlockState originalState,
            IProperty<T> property
    ) {
        return subjectState.with(property, originalState.get(property));
    }
}

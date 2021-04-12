package com.direwolf20.buildinggadgets.common.construction;

import static net.minecraft.state.properties.BlockStateProperties.*;

import net.minecraft.block.BlockState;
import net.minecraft.state.Property;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class should handle anything to do with state modification and allowed states. It is
 * literally the Authority of how the mod handles states :D
 *
 * @todo: Add IMC
 */
public class StateAuthority {
    public static final List<Property<?>> BANNED_STATES = Arrays.asList(
            AGE_1, AGE_2, AGE_3, AGE_5, AGE_7, AGE_15, AGE_25,
            OPEN, POWERED, WATERLOGGED, SNOWY, TRIGGERED, BITES, DISTANCE , EGGS , HATCH, LAYERS,
            LEVEL_CAULDRON, LEVEL_COMPOSTER, LEVEL_FLOWING, LEVEL_HONEY, LEVEL, MOISTURE, NOTE, PICKLES, POWER,
            STAGE, STABILITY_DISTANCE, CHEST_TYPE, BED_PART, NOTEBLOCK_INSTRUMENT, STRUCTUREBLOCK_MODE, BAMBOO_LEAVES,
            EXTENDED, LOCKED
    );

    /**
     * Handles the cleaning of states to ensure we're not copying over potentially
     * abusive states like growth of blocks
     */
    public static BlockState pipe(BlockState state) {
        BlockState subjectState = state.getBlock().defaultBlockState();

        // Remove properties from the state
        Collection<Property<?>> properties = state.getProperties();
        for (Property<?> property : properties) {
            // Skip banned states
            if (BANNED_STATES.contains(property)) {
                continue;
            }

            subjectState = applyProperty(subjectState, state, property);
        }

        // Literally just for leaves when being placed by our mod
        if (state.getProperties().contains(PERSISTENT))
            subjectState = subjectState.setValue(PERSISTENT, true);

        return subjectState;
    }

    /**
     * Required due to reflection. Mutates a states properties by getting the original state value
     * from the original state and applying it to the subject state.
     */
    private static <T extends Comparable<T>> BlockState applyProperty(
            BlockState subjectState,
            BlockState originalState,
            Property<T> property
    ) {
        return subjectState.setValue(property, originalState.getValue(property));
    }
}

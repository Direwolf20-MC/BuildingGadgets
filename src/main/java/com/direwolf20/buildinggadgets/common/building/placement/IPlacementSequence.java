package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.Region;
import net.minecraft.util.math.BlockPos;

/**
 * Represents all block that should be changed for the build.
 * <p>
 * The yielding position should be inside the {@link #getBoundingBox()}. They do not have to be continuous, or yielding
 * in any order, however it should not yield any repeating positions and have a finite number of possible results.
 * </p>
 */
public interface IPlacementSequence extends Iterable<BlockPos> {

    /**
     * The bounding box such that all yielding positions are inside the region.
     */
    Region getBoundingBox();

}

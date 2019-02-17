package com.direwolf20.buildinggadgets.common.tools.gadget.placement;

import com.direwolf20.buildinggadgets.common.tools.Region;

public interface ITargetProvider extends Iterable<PlacementTarget> {

    Region getBoundingBox();

}

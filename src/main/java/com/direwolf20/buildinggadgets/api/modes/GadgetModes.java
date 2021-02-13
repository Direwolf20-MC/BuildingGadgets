package com.direwolf20.buildinggadgets.api.modes;

import com.direwolf20.buildinggadgets.common.building.modes.*;
import com.google.common.collect.Sets;

import java.util.Set;

public class GadgetModes {
    /**
     * The Building Gadget will query this list directly allowing you to actually remove
     * modes from our default list of modes.
     */
    public static Set<IMode> buildingModes = Sets.newHashSet(
        new BuildToMeMode(),
        new VerticalColumnMode(false),
        new HorizontalColumnMode(false),
        new VerticalWallMode(),
        new HorizontalWallMode(),
        new StairMode(),
        new GridMode(false),
        new SurfaceMode(false)
    );

    /**
     * The Exchanging Gadget will query this list directly allowing you to actually remove
     * modes from our default list of modes.
     */
    public static Set<IMode> exchangingModes = Sets.newHashSet(
        new SurfaceMode(true),
        new GridMode(true),
        new VerticalColumnMode(true),
        new HorizontalColumnMode(true)
    );
}

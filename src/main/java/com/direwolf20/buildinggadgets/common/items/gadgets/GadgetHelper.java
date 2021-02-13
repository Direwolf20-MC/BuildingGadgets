package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.building.modes.GridMode;
import com.direwolf20.buildinggadgets.common.building.modes.HorizontalColumnMode;
import com.direwolf20.buildinggadgets.common.building.modes.StairMode;
import com.direwolf20.buildinggadgets.common.building.modes.VerticalColumnMode;

public class GadgetHelper {
    public static int getRangeInBlocks(int range, IMode mode) {
        if( mode instanceof StairMode ||
            mode instanceof VerticalColumnMode ||
            mode instanceof HorizontalColumnMode)
            return range;

        if( mode instanceof GridMode)
            return range < 7 ? 9 : range < 13 ? 11 * 11: 19 * 19;

        return range == 1 ? 1 : (range + 1) * (range + 1);
    }
}

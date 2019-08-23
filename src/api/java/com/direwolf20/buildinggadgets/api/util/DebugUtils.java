package com.direwolf20.buildinggadgets.api.util;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;


public final class DebugUtils {
    public static void printBuildToLog(Logger log, Level level, IBuildView view, boolean printRequiredItems) {
        log.log(level, "Outputting BuildView " + view + " with estimated size " + view.estimateSize() +
                (printRequiredItems ? "and " + view.estimateRequiredItems().getRequiredItems().size() + " required Items." : "."));
        int count = 1;
        for (PlacementTarget target : view) {
            log.log(level, "Found Placement Target " + count++ + " " + target.toString());
        }
        log.log(level, "In total " + (count - 1) + (count == 1 ? " PlacementTarget was found" : " PlacementTargets were found"));
        if (printRequiredItems) {
            for (UniqueItem item : view.estimateRequiredItems().getRequiredItems().elementSet()) {
                log.log(level, "Found " + item + " as a required Item.");
            }
        }
    }
}

package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.api.modes.GadgetModes;
import com.direwolf20.buildinggadgets.api.modes.IMode;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Set;

public class Modes {
    public static Set<IMode> getBuildingModes() {
        return GadgetModes.buildingModes;
    }

    public static Set<IMode> getExchangingModes() {
        return GadgetModes.exchangingModes;
    }

    @Nullable
    public static IMode getFromName(Set<IMode> modes, ResourceLocation identifier) {
        return modes.stream()
            .filter(e -> e.identifier().equals(identifier))
            .findFirst()
            .orElse(null);
    }
}

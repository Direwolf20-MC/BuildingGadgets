package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.building.modes.EmptyMode;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

public class Modes {
    public static Set<IMode> getBuildingModes() {
        return com.direwolf20.buildinggadgets.api.modes.Modes.buildingModes;
    }

    public static Set<IMode> getExchangingModes() {
        return com.direwolf20.buildinggadgets.api.modes.Modes.exchangingModes;
    }

    public static IMode getFromName(Set<IMode> modes, ResourceLocation identifier) {
        IMode result = modes.stream()
            .filter(e -> e.getRegistryName().equals(identifier))
            .findFirst()
            .orElse(null);

        if (result != null) {
            return result;
        }

        // Attempt to get the first from the set
        if (modes.size() == 0) {
            return new EmptyMode(); // Something went drastically wrong
        }

        return modes.iterator().next();
    }
}

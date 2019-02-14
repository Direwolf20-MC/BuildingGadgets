package com.direwolf20.buildinggadgets.common.items.pastes;

import java.util.function.IntSupplier;

import com.direwolf20.buildinggadgets.common.config.Config;

public enum RegularPasteContainerTypes {

    /**
     * Iron paste container
     */
    T1("", () ->  Config.PASTE_CONTAINERS.capacityT1.get()),
    /**
     * Gold paste container
     */
    T2("t2", () -> Config.PASTE_CONTAINERS.capacityT2.get()),
    /**
     * Diamond paste container
     */
    T3("t3", () -> Config.PASTE_CONTAINERS.capacityT3.get());

    public final String itemSuffix;
    public final IntSupplier capacitySupplier;

    RegularPasteContainerTypes(String itemSuffix, IntSupplier capacitySupplier) {
        this.itemSuffix = itemSuffix;
        this.capacitySupplier = capacitySupplier;
    }

}

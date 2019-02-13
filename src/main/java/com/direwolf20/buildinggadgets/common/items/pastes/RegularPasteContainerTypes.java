package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;

import java.util.function.IntSupplier;

public enum RegularPasteContainerTypes {

    /**
     * Iron paste container
     */
    T1("", () -> SyncedConfig.t1ContainerCapacity),
    /**
     * Gold paste container
     */
    T2("t2", () -> SyncedConfig.t2ContainerCapacity),
    /**
     * Diamond paste container
     */
    T3("t3", () -> SyncedConfig.t3ContainerCapacity);

    public final String itemSuffix;
    public final IntSupplier capacitySupplier;

    RegularPasteContainerTypes(String itemSuffix, IntSupplier capacitySupplier) {
        this.itemSuffix = itemSuffix;
        this.capacitySupplier = capacitySupplier;
    }

}

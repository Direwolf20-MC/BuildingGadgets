package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.function.IntSupplier;

public enum RegularPasteContainerTypes {

    /**
     * Iron paste container
     */
    T1("t1", Config.PASTE_CONTAINERS.capacityT1::get),
    /**
     * Gold paste container
     */
    T2("t2", Config.PASTE_CONTAINERS.capacityT2::get),
    /**
     * Diamond paste container
     */
    T3("t3", Config.PASTE_CONTAINERS.capacityT3::get);

    private final String itemSuffix;
    private final IntSupplier capacitySupplier;
    private final ResourceLocation registryName;
    RegularPasteContainerTypes(String itemSuffix, IntSupplier capacitySupplier) {
        this.itemSuffix = itemSuffix;
        this.registryName = new ResourceLocation(BuildingGadgets.MODID,"construction_paste_container_"+itemSuffix);
        this.capacitySupplier = capacitySupplier;
    }

    public String getItemSuffix() {
        return itemSuffix;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public IntSupplier getCapacitySupplier() {
        return capacitySupplier;
    }

    public ConstructionPasteContainer create(Item.Properties builder) {
        return new ConstructionPasteContainer(builder,getCapacitySupplier());
    }
}

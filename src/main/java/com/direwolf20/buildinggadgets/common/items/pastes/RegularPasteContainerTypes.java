package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference.ItemReference;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.function.IntSupplier;

public enum RegularPasteContainerTypes {

    /**
     * Iron paste container
     */
    T1(ItemReference.PASTE_CONTAINER_T1_RL, Config.PASTE_CONTAINERS.capacityT1::get),
    /**
     * Gold paste container
     */
    T2(ItemReference.PASTE_CONTAINER_T1_RL, Config.PASTE_CONTAINERS.capacityT2::get),
    /**
     * Diamond paste container
     */
    T3(ItemReference.PASTE_CONTAINER_T1_RL, Config.PASTE_CONTAINERS.capacityT3::get);

    private final IntSupplier capacitySupplier;
    private final ResourceLocation registryName;

    RegularPasteContainerTypes(ResourceLocation loc, IntSupplier capacitySupplier) {
        this.registryName = loc;
        this.capacitySupplier = capacitySupplier;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public IntSupplier getCapacitySupplier() {
        return capacitySupplier;
    }

    public ConstructionPasteContainer create(Item.Properties builder) {
        return new ConstructionPasteContainer(builder, getCapacitySupplier());
    }
}

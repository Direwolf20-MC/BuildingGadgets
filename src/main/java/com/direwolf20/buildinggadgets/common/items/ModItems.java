package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModItems {
    public static final Item.Properties ITEM_GROUP = new Item.Properties().group(BuildingGadgets.itemGroup);
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, BuildingGadgets.MOD_ID);

    public static final RegistryObject<Item> BUILDING_GADGET = ITEMS.register("building_gadget", BuildingGadget::new);
}

package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class OurItems {
    private OurItems() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);

    // Gadgets
    public static final RegistryObject<Item> BUILDING_GADGET_ITEM = ITEMS.register("gadget_building", BuildingGadgetItem::new);
    public static final RegistryObject<Item> EXCHANGING_GADGET_ITEM = ITEMS.register("gadget_exchanging", ExchangingGadgetItem::new);
    public static final RegistryObject<Item> COPY_PASTE_GADGET_ITEM = ITEMS.register("gadget_copy_paste", CopyGadgetItem::new);
    public static final RegistryObject<Item> DESTRUCTION_GADGET_ITEM = ITEMS.register("gadget_destruction", DestructionGadgetItem::new);

    // Construction Paste Containers
    public static final RegistryObject<Item> PASTE_CONTAINER_T1_ITEM
            = ITEMS.register("construction_paste_container_t1", () -> new ConstructionPasteContainer(false, Config.PASTE_CONTAINERS.capacityT1::get));
    public static final RegistryObject<Item> PASTE_CONTAINER_T2_ITEM
            = ITEMS.register("construction_paste_container_t2", () -> new ConstructionPasteContainer(false, Config.PASTE_CONTAINERS.capacityT2::get));
    public static final RegistryObject<Item> PASTE_CONTAINER_T3_ITEM
            = ITEMS.register("construction_paste_container_t3", () -> new ConstructionPasteContainer(false, Config.PASTE_CONTAINERS.capacityT3::get));
    public static final RegistryObject<Item> PASTE_CONTAINER_CREATIVE_ITEM
            = ITEMS.register("construction_paste_container_creative", () -> new ConstructionPasteContainer(true));

    // Construction Paste
    public static final RegistryObject<Item> CONSTRUCTION_PASTE_ITEM = ITEMS.register("construction_paste", ConstructionPaste::new);
    public static final RegistryObject<Item> CONSTRUCTION_PASTE_DENSE_ITEM = ITEMS.register("construction_chunk_dense", () -> new Item(itemProperties()));

    // Template
    public static final RegistryObject<Item> TEMPLATE_ITEM = ITEMS.register("template", TemplateItem::new);

    // Item Blocks
    public static final RegistryObject<Item> CONSTRUCTION_ITEM
            = ITEMS.register("construction_block", () -> new BlockItem(OurBlocks.CONSTRUCTION_BLOCK.get(), OurItems.itemProperties()));
    public static final RegistryObject<Item> CONSTRUCTION_DENSE_ITEM
            = ITEMS.register("construction_block_dense", () -> new BlockItem(OurBlocks.CONSTRUCTION_DENSE_BLOCK.get(), OurItems.itemProperties()));
    public static final RegistryObject<Item> CONSTRUCTION_POWDER_ITEM
            = ITEMS.register("construction_block_powder", () -> new BlockItem(OurBlocks.CONSTRUCTION_POWDER_BLOCK.get(), OurItems.itemProperties()));
    public static final RegistryObject<Item> TEMPLATE_MANGER_ITEM
            = ITEMS.register("template_manager", () -> new BlockItem(OurBlocks.TEMPLATE_MANGER_BLOCK.get(), OurItems.itemProperties()));

    public static Item.Properties itemProperties() {
        return new Item.Properties().group(BuildingGadgets.creativeTab);
    }

    public static Item.Properties nonStackableItemProperties() {
        return itemProperties().maxStackSize(1);
    }
}

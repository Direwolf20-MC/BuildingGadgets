package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.pastes.RegularPasteContainerTypes;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import com.direwolf20.buildinggadgets.common.utils.Reference;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MODID)
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class BGItems {
    private BGItems() {}

    private static final RegistryContainer<Item, RegistryObjectBuilder<Item, Item.Properties>> container = new RegistryContainer<>();
    // Gadgets
    @ObjectHolder("gadget_building")
    public static Item gadgetBuilding;
    @ObjectHolder("gadget_copy_paste")
    public static Item gadgetCopyPaste;
    @ObjectHolder("gadget_exchanging")
    public static Item gadgetExchanger;
    @ObjectHolder("gadget_destruction")
    public static Item gadgetDestruction;

    // Building Items
    @ObjectHolder("construction_paste")
    public static Item constructionPaste;
    @ObjectHolder("template")
    public static Item template  ;

    // Construction Paste Containers
    @ObjectHolder("construction_paste_container_t1")
    public static Item ConstructionPasteContainer;
    @ObjectHolder("construction_paste_container_t2")
    public static Item ConstructionPasteContainer2;
    @ObjectHolder("construction_paste_container_t3")
    public static Item ConstructionPasteContainer3;
    @ObjectHolder("construction_paste_container_creative")
    public static Item CreativeConstructionPasteContainer;

    static void init() {
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(GadgetExchanger.REGISTRY_NAME).builder(itemProperties()).factory(GadgetExchanger::new));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(GadgetBuilding.REGISTRY_NAME).builder(itemProperties()).factory(GadgetBuilding::new));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(GadgetDestruction.REGISTRY_NAME).builder(itemProperties()).factory(GadgetDestruction::new));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(GadgetCopyPaste.REGISTRY_NAME).builder(itemProperties()).factory(GadgetCopyPaste::new));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(RegularPasteContainerTypes.T1.getRegistryName()).builder(itemProperties()).factory(RegularPasteContainerTypes.T1::create));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(RegularPasteContainerTypes.T2.getRegistryName()).builder(itemProperties()).factory(RegularPasteContainerTypes.T2::create));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(RegularPasteContainerTypes.T3.getRegistryName()).builder(itemProperties()).factory(RegularPasteContainerTypes.T3::create));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(ConstructionPasteContainerCreative.REGISTRY_NAME).builder(itemProperties()).factory(ConstructionPasteContainerCreative::new));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(ConstructionPaste.REGISTRY_NAME).builder(itemProperties()).factory(ConstructionPaste::new));
        container.add(new RegistryObjectBuilder<Item, Item.Properties>(Template.REGISTRY_NAME).builder(itemProperties()).factory(Template::new));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        container.register(event);
    }

    static Item.Properties itemProperties() {
        return new Item.Properties().group(BuildingObjects.creativeTab);
    }

    static Item.Properties itemPropertiesWithoutGroup() {
        return new Item.Properties();
    }

    static void cleanup() {
        container.clear();
    }
}

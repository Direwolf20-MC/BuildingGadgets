package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
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
import net.minecraft.item.Item;
import net.minecraft.item.Item.Builder;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(BuildingGadgets.MODID)
@EventBusSubscriber(modid = BuildingGadgets.MODID, bus = Bus.MOD)
public final class BGItems {
    private BGItems() {}

    private static final RegistryContainer<Item, RegistryObjectBuilder<Item, Builder>> container = new RegistryContainer<>();
    // Gadgets
    @ObjectHolder("gadgets_building")
    public static Item gadgetBuilding;
    @ObjectHolder("gadgets_copy_paste")
    public static Item gadgetCopyPaste;
    @ObjectHolder("gadgets_exchanging")
    public static Item gadgetExchanger;
    @ObjectHolder("gadgets_destruction")
    public static Item gadgetDestruction  ;

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

    public static void init() {
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(GadgetExchanger.REGISTRY_NAME).builder(itemBuilder()).factory(GadgetExchanger::new));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(GadgetBuilding.REGISTRY_NAME).builder(itemBuilder()).factory(GadgetBuilding::new));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(GadgetDestruction.REGISTRY_NAME).builder(itemBuilder()).factory(GadgetDestruction::new));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(GadgetCopyPaste.REGISTRY_NAME).builder(itemBuilder()).factory(GadgetCopyPaste::new));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(RegularPasteContainerTypes.T1.getRegistryName()).builder(itemBuilder()).factory(RegularPasteContainerTypes.T1::create));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(RegularPasteContainerTypes.T2.getRegistryName()).builder(itemBuilder()).factory(RegularPasteContainerTypes.T2::create));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(RegularPasteContainerTypes.T3.getRegistryName()).builder(itemBuilder()).factory(RegularPasteContainerTypes.T3::create));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(ConstructionPasteContainerCreative.REGISTRY_NAME).builder(itemBuilder()).factory(ConstructionPasteContainerCreative::new));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(ConstructionPaste.REGISTRY_NAME).builder(itemBuilder()).factory(ConstructionPaste::new));
        container.add(new RegistryObjectBuilder<Item, Item.Builder>(Template.REGISTRY_NAME).builder(itemBuilder()).factory(Template::new));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        container.register(event);
    }

    static Item.Builder itemBuilder() {
        return new Item.Builder().group(BuildingObjects.creativeTab);
    }
}

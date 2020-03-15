package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.TemplateItem;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.pastes.RegularPasteContainerTypes;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ItemReference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.SaveReference;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class OurItems {
    private OurItems() {}

    // Our Gadgets
    @ObjectHolder(ItemReference.GADGET_BUILDING)
    public static GadgetBuilding gadgetBuilding;

    @ObjectHolder(ItemReference.GADGET_COPY_PASTE)
    public static GadgetCopyPaste gadgetCopyPaste;

    @ObjectHolder(ItemReference.GADGET_EXCHANGING)
    public static GadgetExchanger gadgetExchanger;

    @ObjectHolder(ItemReference.GADGET_DESTRUCTION)
    public static GadgetDestruction gadgetDestruction;

    // Building Items
    @ObjectHolder(ItemReference.CONSTRUCTION_PASTE)
    public static ConstructionPaste constructionPaste;

    @ObjectHolder(ItemReference.CONSTRUCTION_CHUNK_DENSE)
    public static Item constructionChunkDense;

    @ObjectHolder(ItemReference.TEMPLATE)
    public static TemplateItem template;

    // Construction Paste Containers
    @ObjectHolder(ItemReference.PASTE_CONTAINER_T1)
    public static ConstructionPasteContainer constructionPasteContainerT1;

    @ObjectHolder(ItemReference.PASTE_CONTAINER_T2)
    public static ConstructionPasteContainer constructionPasteContainerT2;

    @ObjectHolder(ItemReference.PASTE_CONTAINER_T3)
    public static ConstructionPasteContainer constructionPasteContainerT3;

    @ObjectHolder(ItemReference.PASTE_CONTAINER_CREATIVE)
    public static ConstructionPasteContainerCreative creativeConstructionPasteContainer;

    // Back to how the actual class works.
    private static Set<Builder> itemBuilders = new HashSet<>();

    /**
     * setup us required to be loaded before the registry event
     * happens.
     */
    public static void setup() {
        // Looks complicated but it's not. We're just building a list of Builders that will, when
        // applied will construct the Item.Properties Builder for us allowing us to do a lot less work.
        itemBuilders.addAll(new HashSet<Builder>() {{
            add(new Builder(ItemReference.GADGET_BUILDING_RL).setBuilder(nonStackableItemProperties().maxDamage(1))
                    .setFactory(p -> new GadgetBuilding(p, Config.GADGETS.GADGET_BUILDING.undoSize::get, SaveReference.UNDO_BUILDING)));
            add(new Builder(ItemReference.GADGET_EXCHANGING_RL).setBuilder(nonStackableItemProperties().maxDamage(1))
                    .setFactory(p -> new GadgetExchanger(p, Config.GADGETS.GADGET_EXCHANGER.undoSize::get, SaveReference.UNDO_EXCHANGING)));
            add(new Builder(ItemReference.GADGET_COPY_PASTE_RL).setBuilder(nonStackableItemProperties().maxDamage(1))
                    .setFactory(p -> new GadgetCopyPaste(p, Config.GADGETS.GADGET_COPY_PASTE.undoSize::get, SaveReference.UNDO_COPY_PASTE)));
            add(new Builder(ItemReference.GADGET_DESTRUCTION_RL).setBuilder(nonStackableItemProperties().maxDamage(1))
                    .setFactory(p -> new GadgetDestruction(p, Config.GADGETS.GADGET_DESTRUCTION.undoSize::get, SaveReference.UNDO_DESTRUCTION)));
            add(new Builder(ItemReference.PASTE_CONTAINER_T1_RL).setBuilder(nonStackableItemProperties()).setFactory(RegularPasteContainerTypes.T1::create));
            add(new Builder(ItemReference.PASTE_CONTAINER_T2_RL).setBuilder(nonStackableItemProperties()).setFactory(RegularPasteContainerTypes.T2::create));
            add(new Builder(ItemReference.PASTE_CONTAINER_T3_RL).setBuilder(nonStackableItemProperties()).setFactory(RegularPasteContainerTypes.T3::create));
            add(new Builder(ItemReference.PASTE_CONTAINER_CREATIVE_RL).setBuilder(nonStackableItemProperties()).setFactory(ConstructionPasteContainerCreative::new));
            add(new Builder(ItemReference.CONSTRUCTION_PASTE_RL).setBuilder(itemProperties()).setFactory(ConstructionPaste::new));
            add(new Builder(ItemReference.CONSTRUCTION_CHUNK_DENSE_RL).setBuilder(itemProperties()).setFactory(Item::new));
            add(new Builder(ItemReference.TEMPLATE_RL).setBuilder(itemProperties()).setFactory(TemplateItem::new));
        }});
    }

    /**
     * Register the items with the forge registry by looping over
     * our builders and calling their register method.
     *
     * @param event the forge registry event
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        itemBuilders.forEach(e -> event.getRegistry().register(
                e.createItem()
        ));

        itemBuilders.clear();
    }

    static Item.Properties itemProperties() {
        return new Item.Properties().group(Registries.creativeTab);
    }

    private static Item.Properties nonStackableItemProperties() {
        return itemProperties().maxStackSize(1);
    }

    /**
     * A very simple Builder for item registry to provide
     * forge / mc with our items.
     */
    private static final class Builder {
        private final ResourceLocation registryName;
        private Item.Properties builder;
        private Function<Item.Properties, Item> factory; // Our item

        private Builder(ResourceLocation registryName) {
            this.registryName = registryName;
        }

        private Builder setBuilder(Item.Properties builder) {
            this.builder = builder;
            return this;
        }

        private Builder setFactory(Function<Item.Properties, Item> factory) {
            this.factory = factory;
            return this;
        }

        private Item createItem() {
            return this.factory.apply(this.builder).setRegistryName(this.registryName);
        }
    }
}

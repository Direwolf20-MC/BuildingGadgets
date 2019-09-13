package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.registry.objects.BuildingObjects;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Set;
import java.util.function.Function;

@ObjectHolder(Reference.MODID)
@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OurItems {
    // Warning: ugly object holder code below
    // Our Gadgets
    @ObjectHolder(Reference.ItemReference.GADGET_BUILDING)
    public static GadgetBuilding gadgetBuilding;
    @ObjectHolder(Reference.ItemReference.GADGET_COPY_PASTE)
    public static GadgetCopyPaste gadgetCopyPaste;
    @ObjectHolder(Reference.ItemReference.GADGET_EXCHANGING)
    public static GadgetExchanger gadgetExchanger;
    @ObjectHolder(Reference.ItemReference.GADGET_DESTRUCTION)
    public static GadgetDestruction gadgetDestruction;

    // Building Items
    @ObjectHolder(Reference.ItemReference.CONSTRUCTION_PASTE)
    public static ConstructionPaste constructionPaste;
    @ObjectHolder(Reference.ItemReference.CONSTRUCTION_CHUNK_DENSE)
    public static Item constructionChunkDense;
    @ObjectHolder(Reference.ItemReference.TEMPLATE)
    public static Template template;

    // Construction Paste Containers
    @ObjectHolder(Reference.ItemReference.PASTE_CONTAINER_T1)
    public static ConstructionPasteContainer constructionPasteContainerT1;
    @ObjectHolder(Reference.ItemReference.PASTE_CONTAINER_T2)
    public static ConstructionPasteContainer constructionPasteContainerT2;
    @ObjectHolder(Reference.ItemReference.PASTE_CONTAINER_T3)
    public static ConstructionPasteContainer constructionPasteContainerT3;
    @ObjectHolder(Reference.ItemReference.PASTE_CONTAINER_CREATIVE)
    public static ConstructionPasteContainerCreative creativeConstructionPasteContainer;

    // Back to how the actual class works.
    private static Set<Builder> itemBuilders;

    /**
     * setup us required to be loaded before the registry event
     * happens.
     */
    public static void setup() {
        itemBuilders.add(new Builder(Reference.ItemReference.GADGET_BUILDING_RL)
                    .setBuilder(nonStackableItemProperties().maxDamage(1))
                    .setFactory(GadgetBuilding::new));

        itemBuilders.add(new Builder(Reference.ItemReference.GADGET_EXCHANGING_RL)
                .setBuilder(nonStackableItemProperties().maxDamage(1))
                .setFactory(GadgetExchanger::new));

        itemBuilders.add(new Builder(Reference.ItemReference.GADGET_COPY_PASTE_RL)
                .setBuilder(nonStackableItemProperties().maxDamage(1))
                .setFactory(GadgetExchanger::new));

        itemBuilders.add(new Builder(Reference.ItemReference.GADGET_DESTRUCTION_RL)
                .setBuilder(nonStackableItemProperties().maxDamage(1))
                .setFactory(GadgetDestruction::new));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        itemBuilders.forEach(e -> event.getRegistry().register(
                e.register()
        ));
    }

    private static Item.Properties itemProperties() {
        return new Item.Properties().group(BuildingObjects.creativeTab);
    }

    private static Item.Properties nonStackableItemProperties() {
        return itemProperties().maxStackSize(1);
    }

    /**
     * A very simple Builder for item registry to provide
     * forge / mc with our items.
     */
    private static final class Builder {
        final ResourceLocation registryName;
        Item.Properties builder;
        Function<Item.Properties, Item> factory; // Our item

        public Builder(ResourceLocation registryName) {
            this.registryName = registryName;
        }

        public Builder setBuilder(Item.Properties builder) {
            this.builder = builder;
            return this;
        }

        public Builder setFactory(Function<Item.Properties, Item> factory) {
            this.factory = factory;
            return this;
        }

        public Item register() {
            return this.factory.apply(this.builder).setRegistryName(this.registryName);
        }
    }
}

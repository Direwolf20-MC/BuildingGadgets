package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.api.APIReference.TemplateSerializerReference;
import com.direwolf20.buildinggadgets.api.APIReference.TileDataFactoryReference;
import com.direwolf20.buildinggadgets.api.APIReference.TileDataSerializerReference;
import com.direwolf20.buildinggadgets.api.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.registry.IOrderedRegistry;
import com.direwolf20.buildinggadgets.api.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.SerialisationSupport;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = APIReference.MODID, bus = Bus.MOD)
public final class Registries {

    public static final ResourceLocation STACK_PROVIDER_ITEM_HANDLER = new ResourceLocation("buildinggadgets:stack_provider/item_handler");

    public static final String IMC_METHOD_STACK_PROVIDER = "imc_stack_provider";
    private Registries() {}

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder.create();

    private static IForgeRegistry<ITemplateSerializer> templateSerializers = null;
    private static IForgeRegistry<ITileDataSerializer> tileDataSerializers = null;
    private static IOrderedRegistry<ITileDataFactory> tileDataFactories = null;

    static {
        addDefaultOrdered();
    }

    public static IForgeRegistry<ITemplateSerializer> getTemplateSerializers() {
        Preconditions
                .checkState(templateSerializers != null, "Attempted to retrieve TemplateSerializerRegistry before registries were created!");
        return templateSerializers;
    }

    static void onCreateRegistries() {
        BuildingGadgetsAPI.LOG.trace("Creating ForgeRegistries");
        templateSerializers = new RegistryBuilder<ITemplateSerializer>()
                .setType(ITemplateSerializer.class)
                .setName(TemplateSerializerReference.REGISTRY_ID_TEMPLATE_SERIALIZER)
                .create();
        tileDataSerializers = new RegistryBuilder<ITileDataSerializer>()
                .setType(ITileDataSerializer.class)
                .setName(TileDataSerializerReference.REGISTRY_ID_TILE_DATA_SERIALIZER)
                .create();
        BuildingGadgetsAPI.LOG.trace("Finished Creating ForgeRegistries");
    }

    @SubscribeEvent
    public static void registerTemplateSerializers(RegistryEvent.Register<ITemplateSerializer> event) {
        event.getRegistry().register(new ImmutableTemplate.Serializer().setRegistryName(TemplateSerializerReference.IMMUTABLE_TEMPLATE_SERIALIZER_RL));
    }


    @SubscribeEvent
    public static void registerTileDataSerializers(RegistryEvent.Register<ITileDataSerializer> event) {
        event.getRegistry().register(SerialisationSupport.dummyDataSerializer().setRegistryName(TileDataSerializerReference.DUMMY_SERIALIZER_RL));
        event.getRegistry().register(SerialisationSupport.nbtTileDataSerializer().setRegistryName(TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL));
    }

    static void createOrderedRegistries() {
        BuildingGadgetsAPI.LOG.trace("Creating Ordered Registries");
        tileDataFactories = tileDataFactoryBuilder.build();
        tileDataFactoryBuilder = null;
        BuildingGadgetsAPI.LOG.trace("Finished Creating Ordered Registries");
    }

    static boolean handleIMC(InterModComms.IMCMessage message) {
        if (message.getMethod().equals(TileDataFactoryReference.IMC_METHOD_TILEDATA_FACTORY)) {
            Preconditions.checkState(tileDataFactories != null,
                    "Attempted to register ITileDataFactory, after the Registry has been built!");
            tileDataFactoryBuilder.merge(
                    message.<Supplier<TopologicalRegistryBuilder<ITileDataFactory>>>getMessageSupplier().get().get());
            return true;
        }
        return false;
    }

    private static void addDefaultOrdered() {
        tileDataFactoryBuilder
                .addMarker(APIReference.MARKER_BEFORE_RL)
                .addMarker(APIReference.MARKER_AFTER_RL)
                .addValue(TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL, TileSupport.dataProviderFactory())
                .addDependency(APIReference.MARKER_AFTER_RL, TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL);
    }

    public static final class TileEntityData {
        public static IOrderedRegistry<ITileDataFactory> getTileDataFactories() {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to retrieve TileDataFactoryRegistry before it was created!");
            return tileDataFactories;
        }

        public static IForgeRegistry<ITileDataSerializer> getTileDataSerializers() {
            Preconditions
                    .checkState(tileDataSerializers != null, "Attempted to retrieve TileDataSerializerRegistry before registries were created!");
            return tileDataSerializers;
        }

    }
}

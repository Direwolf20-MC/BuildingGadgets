package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.api.APIReference.*;
import com.direwolf20.buildinggadgets.api.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.materials.inventory.IHandleProvider;
import com.direwolf20.buildinggadgets.api.materials.inventory.IObjectHandle;
import com.direwolf20.buildinggadgets.api.materials.inventory.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.api.materials.inventory.UniqueItem;
import com.direwolf20.buildinggadgets.api.registry.IOrderedRegistry;
import com.direwolf20.buildinggadgets.api.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.SerialisationSupport;
import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate.DelegatingTemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@EventBusSubscriber(modid = APIReference.MODID, bus = Bus.MOD)
public final class Registries {
    private Registries() {}

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder.create();
    private static TopologicalRegistryBuilder<IHandleProvider> handleProviderBuilder = TopologicalRegistryBuilder.create();

    private static IForgeRegistry<ITemplateSerializer> templateSerializers = null;
    private static IForgeRegistry<ITileDataSerializer> tileDataSerializers = null;
    private static IForgeRegistry<IUniqueObjectSerializer> uniqueObjectSerializers = null;
    private static IOrderedRegistry<ITileDataFactory> tileDataFactories = null;
    private static IOrderedRegistry<IHandleProvider> handleProviders = null;

    static {
        addDefaultOrdered();
    }

    public static IForgeRegistry<ITemplateSerializer> getTemplateSerializers() {
        Preconditions
                .checkState(templateSerializers != null, "Attempted to retrieve TemplateSerializerRegistry before registries were created!");
        return templateSerializers;
    }

    @Nullable
    public static ITemplateSerializer getTemplateSerializer(String s) {
        return getTemplateSerializer(new ResourceLocation(s));
    }

    @Nullable
    public static ITemplateSerializer getTemplateSerializer(ResourceLocation location) {
        return getTemplateSerializers().getValue(location);
    }

    public static IForgeRegistry<IUniqueObjectSerializer> getUniqueObjectSerializers() {
        Preconditions
                .checkState(uniqueObjectSerializers != null, "Attempted to retrieve UniqueObjectSerializerRegistry before registries were created!");
        return uniqueObjectSerializers;
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
        uniqueObjectSerializers = new RegistryBuilder<IUniqueObjectSerializer>()
                .setType(IUniqueObjectSerializer.class)
                .setName(UniqueObjectSerializerReference.REGISTRY_ID_UNIQUE_OBJECT_SERIALIZER)
                .create();
        BuildingGadgetsAPI.LOG.trace("Finished Creating ForgeRegistries");
    }

    @SubscribeEvent
    public static void registerTemplateSerializers(RegistryEvent.Register<ITemplateSerializer> event) {
        BuildingGadgetsAPI.LOG.trace("Registering Template Serializers");
        event.getRegistry().register(new ImmutableTemplate.Serializer().setRegistryName(TemplateSerializerReference.IMMUTABLE_TEMPLATE_SERIALIZER_RL));
        event.getRegistry().register(new DelegatingTemplateSerializer().setRegistryName(TemplateSerializerReference.DELEGATING_TEMPLATE_SERIALIZER_RL));
        BuildingGadgetsAPI.LOG.trace("Finished Registering Template Serializers");
    }

    @SubscribeEvent
    public static void registerTileDataSerializers(RegistryEvent.Register<ITileDataSerializer> event) {
        BuildingGadgetsAPI.LOG.trace("Registering Template Serializers");
        event.getRegistry().register(SerialisationSupport.dummyDataSerializer().setRegistryName(TileDataSerializerReference.DUMMY_SERIALIZER_RL));
        event.getRegistry().register(SerialisationSupport.nbtTileDataSerializer().setRegistryName(TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL));
        BuildingGadgetsAPI.LOG.trace("Finished Registering Template Serializers");
    }

    public static void registerUniqueObjectSerializers(RegistryEvent.Register<IUniqueObjectSerializer> event) {
        BuildingGadgetsAPI.LOG.trace("Registering UniqueObject Serializers");
        event.getRegistry().register(new UniqueItem.Serializer().setRegistryName(UniqueObjectSerializerReference.SIMPLE_UNIQUE_ITEM_ID_RL));
        BuildingGadgetsAPI.LOG.trace("Finished Registering UniqueObject Serializers");
    }

    static void createOrderedRegistries() {
        BuildingGadgetsAPI.LOG.trace("Creating Ordered Registries");
        Preconditions.checkState(tileDataFactoryBuilder != null, "Cannot create Ordered Registries twice!");
        tileDataFactories = tileDataFactoryBuilder.build();
        tileDataFactoryBuilder = null;
        handleProviders = handleProviderBuilder.build();
        handleProviderBuilder = null;
        BuildingGadgetsAPI.LOG.trace("Finished Creating Ordered Registries");
    }

    static boolean handleIMC(InterModComms.IMCMessage message) {
        BuildingGadgetsAPI.LOG.debug("Received IMC message using Method {} from {}.", message.getMethod(), message.getSenderModId());
        if (message.getMethod().equals(TileDataFactoryReference.IMC_METHOD_TILEDATA_FACTORY)) {
            BuildingGadgetsAPI.LOG.debug("Recognized ITileDataFactory registration message. Registering.");
            Preconditions.checkState(tileDataFactoryBuilder != null,
                    "Attempted to register ITileDataFactory, after the Registry has been built!");
            TopologicalRegistryBuilder<ITileDataFactory> builder = message.<Supplier<TopologicalRegistryBuilder<ITileDataFactory>>>getMessageSupplier().get().get();
            tileDataFactoryBuilder.merge(builder);
            BuildingGadgetsAPI.LOG.trace("Registered {} from {} to the ITileDataFactory registry.", builder, message.getSenderModId());
            return true;
        } else if (message.getMethod().equals(HandleProviderReference.IMC_METHOD_HANDLE_PROVIDER)) {
            BuildingGadgetsAPI.LOG.debug("Recognized IHandleProvider registration message. Registering.");
            Preconditions.checkState(handleProviderBuilder != null,
                    "Attempted to register IHandleProvider, after the Registry has been built!");
            TopologicalRegistryBuilder<IHandleProvider> builder = message.<Supplier<TopologicalRegistryBuilder<IHandleProvider>>>getMessageSupplier().get().get();
            handleProviderBuilder.merge(builder);
            BuildingGadgetsAPI.LOG.trace("Registered {} from {} to the IHandleProvider registry.", builder, message.getSenderModId());
            return true;
        }
        return false;
    }

    private static void addDefaultOrdered() {
        tileDataFactoryBuilder
                .addMarker(APIReference.MARKER_BEFORE_RL)
                .addMarker(APIReference.MARKER_AFTER_RL)
                .addValue(TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL, TileSupport.dataProviderFactory())
                .addDependency(APIReference.MARKER_AFTER_RL, TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL)
                .addDependency(APIReference.MARKER_BEFORE_RL, APIReference.MARKER_AFTER_RL);
        handleProviderBuilder
                .addMarker(APIReference.MARKER_BEFORE_RL)
                .addMarker(APIReference.MARKER_AFTER_RL)
                .addDependency(APIReference.MARKER_BEFORE_RL, APIReference.MARKER_AFTER_RL);
    }

    public static final class TileEntityData {
        private TileEntityData() {}

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

    public static final class HandleProvider {
        private HandleProvider() {}

        public static IOrderedRegistry<IHandleProvider> getHandleProviders() {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to retrieve HandleProviderRegistry before it was created!");
            return handleProviders;
        }

        public static boolean indexCapProvider(ICapabilityProvider provider, Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> indexMap) {
            Set<Class<?>> evaluatedClasses = new HashSet<>();
            boolean indexed = false;
            for (IHandleProvider handleProvider : getHandleProviders().getValuesInOrder()) {
                indexed |= handleProvider.index(provider, indexMap, evaluatedClasses);
            }
            return indexed;
        }
    }
}

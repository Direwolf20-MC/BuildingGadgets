package com.direwolf20.buildinggadgets.common.tainted.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IHandleProvider;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class Registries {
    // Registry keys
    public static final ResourceKey<Registry<ITileDataSerializer>> TILE_DATA_SERIALIZERS_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Reference.MODID, "tile_data/serializer"));
    public static final ResourceKey<Registry<IUniqueObjectSerializer>> UNIQUE_OBJECT_SERIALIZERS_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Reference.MODID, "unique_object/serializer"));

    // Actual thing we register too
    public static final DeferredRegister<ITileDataSerializer> TILE_DATA_SERIALIZER_DEFERRED_REGISTER = DeferredRegister.create(TILE_DATA_SERIALIZERS_KEY, Reference.MODID);
    public static final DeferredRegister<IUniqueObjectSerializer> UNIQUE_OBJECT_SERIALIZER_DEFERRED_REGISTER = DeferredRegister.create(UNIQUE_OBJECT_SERIALIZERS_KEY, Reference.MODID);

    // Create the custom registries
    public static final Supplier<IForgeRegistry<ITileDataSerializer>> TILE_DATA_SERIALIZER_REGISTRY = TILE_DATA_SERIALIZER_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<ITileDataSerializer>().disableSaving().disableSync());
    public static final Supplier<IForgeRegistry<IUniqueObjectSerializer>> UNIQUE_DATA_SERIALIZER_REGISTRY = UNIQUE_OBJECT_SERIALIZER_DEFERRED_REGISTER.makeRegistry(() -> new RegistryBuilder<IUniqueObjectSerializer>().disableSaving().disableSync());

    // The things we're registering
    public static final RegistryObject<ITileDataSerializer> NBT_TILE_DATA_SERIALIZER = TILE_DATA_SERIALIZER_DEFERRED_REGISTER.register("nbt_tile_data_serializer", SerialisationSupport::createNbtSerializer);
    public static final RegistryObject<ITileDataSerializer> DUMMY_TILE_DATA_SERIALIZER = TILE_DATA_SERIALIZER_DEFERRED_REGISTER.register("dummy_serializer", SerialisationSupport::createDummySerializer);
    public static final RegistryObject<IUniqueObjectSerializer> UNIQUE_OBJECT_SERIALIZER = UNIQUE_OBJECT_SERIALIZER_DEFERRED_REGISTER.register("simple_item", UniqueItem.Serializer::new);

    private Registries() {
    }

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder.create();
    private static TopologicalRegistryBuilder<IHandleProvider> handleProviderBuilder = TopologicalRegistryBuilder.create();

    private static ImmutableOrderedRegistry<ITileDataFactory> tileDataFactories = null;
    private static ImmutableOrderedRegistry<IHandleProvider> handleProviders = null;

    static {
        addDefaultOrdered();
    }

    public static void createOrderedRegistries() {
        BuildingGadgets.LOG.trace("Creating Ordered Registries");
        Preconditions.checkState(tileDataFactoryBuilder != null, "Cannot create Ordered Registries twice!");
        tileDataFactories = tileDataFactoryBuilder.build();
        tileDataFactoryBuilder = null;
        handleProviders = handleProviderBuilder.build();
        handleProviderBuilder = null;
        BuildingGadgets.LOG.trace("Finished Creating Ordered Registries");
    }

    public static boolean handleIMC(InterModComms.IMCMessage message) {
        BuildingGadgets.LOG.debug("Received IMC message using Method {} from {}.", message.getMethod(), message.getSenderModId());
        if (message.method().equals(Reference.TileDataFactoryReference.IMC_METHOD_TILEDATA_FACTORY)) {
            BuildingGadgets.LOG.debug("Recognized ITileDataFactory registration message. Registering.");
            Preconditions.checkState(tileDataFactoryBuilder != null,
                    "Attempted to register ITileDataFactory, after the Registry has been built!");
            TopologicalRegistryBuilder<ITileDataFactory> builder = message.<Supplier<TopologicalRegistryBuilder<ITileDataFactory>>>getMessageSupplier().get().get();
            tileDataFactoryBuilder.merge(builder);
            BuildingGadgets.LOG.trace("Registered {} from {} to the ITileDataFactory registry.", builder, message.getSenderModId());
            return true;
        } else if (message.method().equals(Reference.HandleProviderReference.IMC_METHOD_HANDLE_PROVIDER)) {
            BuildingGadgets.LOG.debug("Recognized IHandleProvider registration message. Registering.");
            Preconditions.checkState(handleProviderBuilder != null,
                    "Attempted to register IHandleProvider, after the Registry has been built!");
            TopologicalRegistryBuilder<IHandleProvider> builder = message.<Supplier<TopologicalRegistryBuilder<IHandleProvider>>>getMessageSupplier().get().get();
            handleProviderBuilder.merge(builder);
            BuildingGadgets.LOG.trace("Registered {} from {} to the IHandleProvider registry.", builder, message.getSenderModId());
            return true;
        }
        return false;
    }

    private static void addDefaultOrdered() {
        tileDataFactoryBuilder
                .addMarker(Reference.MARKER_BEFORE_RL)
                .addMarker(Reference.MARKER_AFTER_RL)
                .addValue(Reference.TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL, TileSupport.dataProviderFactory())
                .addDependency(Reference.MARKER_AFTER_RL, Reference.TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL)
                .addDependency(Reference.MARKER_BEFORE_RL, Reference.MARKER_AFTER_RL);
        handleProviderBuilder
                .addMarker(Reference.MARKER_BEFORE_RL)
                .addMarker(Reference.MARKER_AFTER_RL)
                .addDependency(Reference.MARKER_BEFORE_RL, Reference.MARKER_AFTER_RL);
    }

    public static final class TileEntityData {
        private TileEntityData() {
        }

        public static ImmutableOrderedRegistry<ITileDataFactory> getTileDataFactories() {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to retrieve TileDataFactoryRegistry before it was created!");
            return tileDataFactories;
        }
    }

    public static final class HandleProvider {
        private HandleProvider() {
        }

        public static ImmutableOrderedRegistry<IHandleProvider> getHandleProviders() {
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

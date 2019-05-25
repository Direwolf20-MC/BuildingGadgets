package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.api.inventory.IStackProvider;
import com.direwolf20.buildinggadgets.api.registry.IOrderedRegistry;
import com.direwolf20.buildinggadgets.api.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public final class Registries {
    public static final ResourceLocation REGISTRY_ID_TEMPLATE_SERIALIZER = new ResourceLocation("buildinggadgets:template/serializer");
    public static final ResourceLocation REGISTRY_ID_TILE_DATA_SERIALIZER = new ResourceLocation("buildinggadgets:tile_data/serializer");

    public static final String IMC_METHOD_TILEDATA_FACTORY = "imc_tile_data_factory";
    public static final String IMC_METHOD_STACK_PROVIDER = "imc_stack_provider";
    private Registries() {}

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder
            .create();
    private static TopologicalRegistryBuilder<IStackProvider> stackProviderBuilder = TopologicalRegistryBuilder
            .create();

    private static IForgeRegistry<ITemplateSerializer> templateSerializers = null;
    private static IForgeRegistry<ITileDataSerializer> tileDataSerializers = null;
    private static IOrderedRegistry<ITileDataFactory> tileDataFactories = null;
    private static IOrderedRegistry<IStackProvider> stackProviders = null;

    public static IForgeRegistry<ITemplateSerializer> getTemplateSerializers() {
        Preconditions
                .checkState(templateSerializers != null, "Attempted to retrieve TemplateSerializerRegistry before registries were created!");
        return templateSerializers;
    }

    public static IForgeRegistry<ITileDataSerializer> getTileDataSerializers() {
        Preconditions
                .checkState(tileDataSerializers != null, "Attempted to retrieve TileDataSerializerRegistry before registries were created!");
        return tileDataSerializers;
    }

    public static IOrderedRegistry<ITileDataFactory> getTileDataFactories() {
        Preconditions
                .checkState(tileDataFactories != null, "Attempted to retrieve TileDataFactoryRegistry before it was created!");
        return tileDataFactories;
    }

    public static IOrderedRegistry<IStackProvider> getStackProviders() {
        Preconditions
                .checkState(stackProviders != null, "Attempted to retrieve StackProviderRegistry before it was created!");
        return stackProviders;
    }

    static void onCreateRegistries(final IEventBus forgeEventBus) {
        templateSerializers = new RegistryBuilder<ITemplateSerializer>()
                .setType(ITemplateSerializer.class)
                .setName(REGISTRY_ID_TEMPLATE_SERIALIZER)
                .create();
        tileDataSerializers = new RegistryBuilder<ITileDataSerializer>()
                .setType(ITileDataSerializer.class)
                .setName(REGISTRY_ID_TILE_DATA_SERIALIZER)
                .create();
    }

    static void createOrderedRegistries() {
        tileDataFactories = tileDataFactoryBuilder.build();
        stackProviders = stackProviderBuilder.build();
        tileDataFactoryBuilder = null;
        stackProviderBuilder = null;
    }

    static boolean handleIMC(InterModComms.IMCMessage message) {
        if (message.getMethod().equals(IMC_METHOD_TILEDATA_FACTORY)) {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to register ITileDataFactory, after the Registry has been built!");
            tileDataFactoryBuilder
                    .merge(message.<TopologicalRegistryBuilder<ITileDataFactory>>getMessageSupplier().get());
            return true;
        }
        else if (message.getMethod().equals(IMC_METHOD_STACK_PROVIDER)) {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to register IStackProvider, after the Registry has been built!");
            stackProviderBuilder.merge(message.<TopologicalRegistryBuilder<IStackProvider>>getMessageSupplier().get());
            return true;
        }
        return false;
    }
}

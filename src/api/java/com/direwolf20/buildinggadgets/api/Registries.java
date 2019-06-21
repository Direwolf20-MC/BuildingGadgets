package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import com.direwolf20.buildinggadgets.api.inventory.IStackProvider;
import com.direwolf20.buildinggadgets.api.registry.IOrderedRegistry;
import com.direwolf20.buildinggadgets.api.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.DummyTileEntityData;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.template.serialisation.TileDataSerializers;
import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = "buildinggadgets", bus = Bus.MOD)
public final class Registries {
    public static final ResourceLocation REGISTRY_ID_TEMPLATE_SERIALIZER = new ResourceLocation("buildinggadgets:template/serializer");
    public static final ResourceLocation REGISTRY_ID_TILE_DATA_SERIALIZER = new ResourceLocation("buildinggadgets:tile_data/serializer");

    public static final ResourceLocation STACK_PROVIDER_ITEM_HANDLER = new ResourceLocation("buildinggadgets:stack_provider/item_handler");

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

    static {
        addDefaultStackProviders();
    }

    public static IForgeRegistry<ITemplateSerializer> getTemplateSerializers() {
        Preconditions
                .checkState(templateSerializers != null, "Attempted to retrieve TemplateSerializerRegistry before registries were created!");
        return templateSerializers;
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

    @SubscribeEvent
    public static void registerTemplateSerializers(RegistryEvent.Register<ITemplateSerializer> event) {

    }


    @SubscribeEvent
    public static void registerTileDataSerializers(RegistryEvent.Register<ITileDataSerializer> event) {
        event.getRegistry().register(TileDataSerializers.dummyDataSerializer());
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

    private static void addDefaultStackProviders() {
        stackProviderBuilder.addValue(STACK_PROVIDER_ITEM_HANDLER, stack -> {
            if (! stack.isEmpty()) {
                LazyOptional<IItemHandler> handlerCap = stack
                        .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                if (handlerCap.isPresent()) {
                    IItemHandler itemHandler = handlerCap.orElseThrow(IllegalStateException::new);
                    NonNullList<ItemStack> res = NonNullList.create();
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        res.add(itemHandler.getStackInSlot(i));
                    }
                    return res;
                }
            }
            return NonNullList.withSize(0, ItemStack.EMPTY);
        });
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

        public static ITileEntityData createTileData(IWorld world, BlockPos pos) {
            TileEntity te = world.getTileEntity(pos);
            if (te == null)
                return DummyTileEntityData.INSTANCE;
            ITileEntityData res = null;
            for (ITileDataFactory factory : getTileDataFactories()) {
                res = factory.createDataFor(te);
                if (res != null)
                    return res;
            }
            return DummyTileEntityData.INSTANCE;
        }

        public static BlockData createBlockData(IWorld world, BlockPos pos) {
            return new BlockData(world.getBlockState(pos), createTileData(world, pos));
        }
    }
}

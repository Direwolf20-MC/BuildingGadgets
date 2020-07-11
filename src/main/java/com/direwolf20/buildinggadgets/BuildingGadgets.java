package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.client.Events;
import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.common.commands.ClearUndoStoreCommand;
import com.direwolf20.buildinggadgets.common.commands.SpawnBlocksCommand;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.packets.Packets;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BuildingGadgets.MOD_ID)
public class BuildingGadgets
{
    public static final String MOD_ID = "buildinggadgets";
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * Used for the creative tab
     */
    public static ItemGroup itemGroup = new ItemGroup(BuildingGadgets.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.BUILDING_GADGET.get());
        }
    };

    /**
     * Set the mod up
     */
    public BuildingGadgets() {
        // Config setup
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        // Events
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.addListener(this::serverLoad);
        MinecraftForge.EVENT_BUS.register(Events.class);

        ModItems.ITEMS.register(eventBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        Packets.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        KeyBindings.init();
    }

    private void serverLoad(FMLServerStartingEvent event) {
        LOGGER.debug("Registering commands");
        event.getCommandDispatcher().register(
                Commands.literal(MOD_ID)
                        .then(SpawnBlocksCommand.register())
                        .then(ClearUndoStoreCommand.register())
        );
    }

    // Todo: implement this
    private void enqueueIMC(final InterModEnqueueEvent event)
    {
//        // some example code to dispatch IMC to another mod
//        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
//        // some example code to receive and process InterModComms from other mods
//        LOGGER.info("Got IMC {}", event.getIMCStream().
//                map(m->m.getMessageSupplier().get()).
//                collect(Collectors.toList()));
    }
}

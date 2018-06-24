package com.direwolf20.buildinggadgets;


import com.direwolf20.buildinggadgets.Blocks.EffectBlock;
import com.direwolf20.buildinggadgets.Items.BuildingTool;
import com.direwolf20.buildinggadgets.Items.ExchangerTool;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber
public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.init();
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new EffectBlock());
        //event.getRegistry().register(new TurretLaser());
        //GameRegistry.registerTileEntity(GooBlockTileEntity.class, BuildingGadgets.MODID + "_gooblock");
        //GameRegistry.registerTileEntity(TurretLaserTileEntity.class, BuildingGadgets.MODID + "_turretblock");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(ModBlocks.effectBlock).setRegistryName(ModBlocks.effectBlock.getRegistryName()));
        //event.getRegistry().register(new ItemBlock(ModBlocks.turretLaser).setRegistryName(ModBlocks.turretLaser.getRegistryName()));
        event.getRegistry().register(new BuildingTool());
        event.getRegistry().register(new ExchangerTool());
    }
}
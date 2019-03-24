package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.common.commands.DeleteBlockMapsCommand;
import com.direwolf20.buildinggadgets.common.commands.FindBlockMapsCommand;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;


@Mod(modid = BuildingGadgets.MODID, name = BuildingGadgets.MODNAME, version = BuildingGadgets.VERSION, updateJSON = BuildingGadgets.UPDATE_JSON, dependencies = BuildingGadgets.DEPENDENCIES, useMetadata = true)
public class BuildingGadgets {
    public static final String MODID = "buildinggadgets";
    public static final String MODNAME = "Building Gadgets";
    public static final String VERSION = "@VERSION@";
    public static final String UPDATE_JSON = "@UPDATE@";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.3.2694,)";

    public static final CreativeTabs BUILDING_CREATIVE_TAB = new CreativeTabs(new TextComponentTranslation("buildingGadgets").getUnformattedComponentText()) {
        @Override
        public ItemStack getTabIconItem() {
            ItemStack stack = new ItemStack(ModItems.gadgetBuilding);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setByte("creative", (byte) 0); 
            return stack;
        }
    };

    @SidedProxy(clientSide = "com.direwolf20.buildinggadgets.client.proxy.ClientProxy", serverSide = "com.direwolf20.buildinggadgets.common.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static BuildingGadgets instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(@SuppressWarnings("unused") FMLInitializationEvent e) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(@SuppressWarnings("unused") FMLPostInitializationEvent e) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new FindBlockMapsCommand());
        event.registerServerCommand(new DeleteBlockMapsCommand());
    }
}

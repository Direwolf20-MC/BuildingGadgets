package com.direwolf20.buildinggadgets.config;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketSyncConfig;
import com.direwolf20.buildinggadgets.tools.ReflectionTool;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class InGameConfig {
    private static final String KEY_NAME = "NAME";
    static final String KEY_VALUE = "VAL";
    static final String KEY_TYPE = "ID";
    /*@SyncedConfig
    public static boolean     bool = false;
    @SyncedConfig
    public static boolean[]   boolA = {false, true};
    @SyncedConfig
    public static double      dbl = 1.0d;
    @SyncedConfig
    public static double[]    dblA = {1.0d, 2.0d};
    @SyncedConfig
    public static char        chr = 'a';
    @SyncedConfig
    public static char[]      chrA = {'a', 'b'};
    @SyncedConfig
    public static int         int_ = 1;
    @SyncedConfig
    public static int[]       intA = {1, 2};
    @SyncedConfig
    public static String      Str = "STRING!";
    @SyncedConfig
    public static String[]    StrA = {"STR", "ING!"};*/

    @SyncedConfig
    public static double rayTraceRange;

    @SyncedConfig
    public static int maxRange;

    @SyncedConfig
    public static boolean allowAnchor;

    @SyncedConfig
    public static String[] buildingModeWhiteList;

    @SyncedConfig
    public static String[] buildingModeBlackList;

    public static boolean isActiveBuildingMode(BuildingTool.toolModes mode) {
        //not very sofisticated - but this will do
        if (buildingModeWhiteList.length>0) {
            for (String s: buildingModeWhiteList) {
                if (s.equalsIgnoreCase(mode.name()))
                    return true;
            }
            return false;
        } else if (buildingModeBlackList.length>0){
            for (String s: buildingModeBlackList) {
                if (s.equalsIgnoreCase(mode.name()))
                    return false;
            }
        }
        return true;
    }

    @SyncedConfig
    public static String[] buildingBlockWhiteList;

    @SyncedConfig
    public static String[] buildingBlockBlackList;

    public static boolean canBuildBlock(Block block) {
        //TODO: Add oredict support
        ResourceLocation regName = block.getRegistryName();
        if (regName==null)
            return true;
        if (buildingBlockWhiteList.length>0) {
            for (String s: buildingBlockWhiteList) {
                ResourceLocation location = new ResourceLocation(s);
                if (location.equals(regName))
                    return true;
            }
            return false;
        } else if (buildingBlockBlackList.length>0){
            for (String s: buildingBlockBlackList) {
                ResourceLocation location = new ResourceLocation(s);
                if (location.equals(regName))
                    return false;
            }
        }
        return true;
    }

    @SyncedConfig
    public static String[] exchangingModeWhiteList;

    @SyncedConfig
    public static String[] exchangingModeBlackList;

    public static boolean isActiveExchangingMode(ExchangerTool.toolModes mode) {
        //not very sofisticated - but this will do
        if (exchangingModeWhiteList.length>0) {
            for (String s: exchangingModeWhiteList) {
                if (s.equalsIgnoreCase(mode.name()))
                    return true;
            }
            return false;
        } else if (exchangingModeBlackList.length>0){
            for (String s: exchangingModeBlackList) {
                if (s.equalsIgnoreCase(mode.name()))
                    return false;
            }
        }
        return true;
    }

    public static String[] exchangeBlockWhiteList;

    public static String[] exchangeBlockBlackList;

    public static boolean canExchangeBlock(Block block) {
        //TODO: Add oredict support
        ResourceLocation regName = block.getRegistryName();
        if (regName==null)
            return true;
        if (exchangeBlockWhiteList.length>0) {
            for (String s: exchangeBlockWhiteList) {
                ResourceLocation location = new ResourceLocation(s);
                if (location.equals(regName))
                    return true;
            }
            return false;
        } else if (exchangeBlockBlackList.length>0){
            for (String s: exchangeBlockBlackList) {
                ResourceLocation location = new ResourceLocation(s);
                if (location.equals(regName))
                    return false;
            }
        }
        return true;
    }

    public static String[] changeBlockWhiteList;

    public static String[] changeBlockBlackList;

    public static boolean canChangeWithBlock(Block block) {
        //TODO: Add oredict support
        ResourceLocation regName = block.getRegistryName();
        if (regName==null)
            return true;
        if (changeBlockWhiteList.length>0) {
            for (String s: changeBlockWhiteList) {
                ResourceLocation location = new ResourceLocation(s);
                if (location.equals(regName))
                    return true;
            }
            return false;
        } else if (changeBlockBlackList.length>0){
            for (String s: changeBlockBlackList) {
                ResourceLocation location = new ResourceLocation(s);
                if (location.equals(regName))
                    return false;
            }
        }
        return true;
    }

    public static void init() {
        rayTraceRange = Config.rayTraceRange;

        maxRange = Config.subCategoryGadgets.maxRange;
        allowAnchor = Config.subCategoryGadgets.allowAnchors;

        buildingModeWhiteList = Config.subCategoryGadgets.subCategoryBuildingGadget.modeWhiteList;
        buildingModeBlackList = Config.subCategoryGadgets.subCategoryBuildingGadget.modeBlackList;
        buildingBlockWhiteList = Config.subCategoryGadgets.subCategoryBuildingGadget.setBlockWhiteList;
        buildingBlockBlackList = Config.subCategoryGadgets.subCategoryBuildingGadget.setBlockBlackList;

        exchangingModeWhiteList = Config.subCategoryGadgets.subCategoryExchanger.modeWhiteList;
        exchangingModeBlackList = Config.subCategoryGadgets.subCategoryExchanger.modeBlackList;
        exchangeBlockWhiteList = Config.subCategoryGadgets.subCategoryExchanger.exchangeBlockWhiteList;
        exchangeBlockBlackList = Config.subCategoryGadgets.subCategoryExchanger.exchangeBlockBlackList;
        changeBlockWhiteList = Config.subCategoryGadgets.subCategoryExchanger.changeBlockWhiteList;
        changeBlockBlackList = Config.subCategoryGadgets.subCategoryExchanger.changeBlockBlackList;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            BuildingGadgets.logger.info("Sending InGameConfig to freshly logged in client.");
            PacketHandler.INSTANCE.sendTo(new PacketSyncConfig(parseSynchronisation()), (EntityPlayerMP) event.player);
        }
    }

    public static NBTTagCompound parseSynchronisation() {
        NBTTagList list = new NBTTagList();
        List<Field> fields = Collections.unmodifiableList(getSyncFields());
        for (Field field: fields) {
            NBTTagCompound compound = parseField(field);
            if (compound!=null) list.appendTag(compound);
        }
        NBTTagCompound compound = new NBTTagCompound(); //parsed in PacketSyncConfig => requires an TagCompound;
        compound.setTag(KEY_VALUE,list);
        return compound;
    }

    public static void onReadSynchronisation(NBTTagCompound compound) {
        if (!compound.hasKey(KEY_VALUE))
            return;
        NBTTagList list = (NBTTagList) compound.getTag(KEY_VALUE);
        List<Field> fields = Collections.unmodifiableList(getSyncFields());
        Map<String,Field> map = new HashMap<>();
        for (Field f: fields) {
            map.put(getSyncName(f),f);
        }
        for (NBTBase rawNBT: list) {
            if (rawNBT instanceof NBTTagCompound) {
                handleNBT((NBTTagCompound) rawNBT,map);
            } else {
                BuildingGadgets.logger.warn("Unexpected "+rawNBT.getClass().getName()+" found in NBTTagList which was expected to only contain NBTTagCompounds!");
            }
        }
        /*
        BuildingGadgets.logger.info(bool);
        BuildingGadgets.logger.info(Arrays.toString(boolA));
        BuildingGadgets.logger.info(dbl);
        BuildingGadgets.logger.info(Arrays.toString(dblA));
        BuildingGadgets.logger.info(chr);
        BuildingGadgets.logger.info(Arrays.toString(chrA));
        BuildingGadgets.logger.info(int_);
        BuildingGadgets.logger.info(Arrays.toString(intA));
        BuildingGadgets.logger.info(Str);
        BuildingGadgets.logger.info(Arrays.toString(StrA));*/
    }

    private static NBTTagCompound parseField(Field field) {
        try {
            field.setAccessible(true); //should not be needed, but better safe than sorry
            Object value = field.get(null);
            NBTBase valueTag = ConfigType.getValueTag(value);
            if (valueTag==null) {
                BuildingGadgets.logger.warn("Could not use type of Field "+field.getName()+"!"+" Found type "+field.getType().getName()+"!");
                return null;
            }
            String name = getSyncName(field);
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag(KEY_VALUE,valueTag);
            compound.setString(KEY_NAME,name);
            return compound;

        } catch (IllegalAccessException e) {
            BuildingGadgets.logger.error("Failed to parse Field "+field.getName()+" in order to parse ConfigSync data!",e);
        }
        return null;
    }

    private static void handleNBT(NBTTagCompound compound, Map<String,Field> fields) {
        if (!compound.hasKey(KEY_NAME) || !compound.hasKey(KEY_VALUE)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an inproperly initialised NBTTagCompound!");
            return;
        }
        String name = compound.getString(KEY_NAME);
        NBTBase rawValue = compound.getTag(KEY_VALUE);
        if (!(rawValue instanceof NBTTagCompound)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an inproperly initialised NBTTagCompound!");
            return;
        }
        NBTTagCompound value = (NBTTagCompound) rawValue;
        if (!fields.containsKey(name)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an unknown Field!");
            return;
        }
        ConfigType.handleValueTag(fields.get(name),value);
    }

    private static List<Field> getSyncFields() {
        return ReflectionTool.getFilteredFields(InGameConfig.class, field -> field.isAnnotationPresent(SyncedConfig.class) && ReflectionTool.PREDICATE_STATIC.test(field));
    }

    private static String getSyncName(Field field) {
        String name = field.getAnnotation(SyncedConfig.class).value();
        return name.isEmpty()?field.getName():name;
    }

    static {
        init();//ensure that everything is initialised after the class was loaded
    }
}

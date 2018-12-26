package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketSyncConfig;
import com.direwolf20.buildinggadgets.common.tools.ReflectionTool;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID)
public class InGameConfig {
    private static final String KEY_NAME = "NAME";
    private static final String KEY_VALUE = "VAL";

    @SyncedConfig
    public static int energyCostBuilder;

    @SyncedConfig
    public static int energyCostExchanger;

    @SyncedConfig
    public static int energyCostDestruction;

    @SyncedConfig
    public static int energyCostCopyPaste;

    @SyncedConfig
    public static int energyMax;

    @SyncedConfig
    public static int energyMaxDestruction;

    @SyncedConfig
    public static boolean poweredByFE;

    @SyncedConfig
    public static int durabilityBuilder;

    @SyncedConfig
    public static int durabilityExchanger;

    @SyncedConfig
    public static int durabilityDestruction;

    @SyncedConfig
    public static int durabilityCopyPaste;

    @SyncedConfig
    public static double rayTraceRange;

    @SyncedConfig
    public static int maxRange;

    @SyncedConfig
    public static boolean enableDestructionGadget;

    @SyncedConfig
    public static boolean absoluteCoordDefault;

    @SyncedConfig
    public static boolean canOverwriteBlocks;

    @SyncedConfig
    public static boolean enablePaste;

    @SyncedConfig
    public static String[] blockBlacklist;


    public static void init() {
        rayTraceRange = Config.rayTraceRange;
        poweredByFE = Config.poweredByFE;
        enableDestructionGadget = Config.enableDestructionGadget;
        absoluteCoordDefault = Config.absoluteCoordDefault;
        canOverwriteBlocks = Config.canOverwriteBlocks;
        enablePaste = Config.enablePaste;

        blockBlacklist = Config.subCategoryBlacklist.blockBlacklist;

        maxRange = Config.subCategoryGadgets.maxRange;
        energyMax = Config.subCategoryGadgets.maxEnergy;

        energyCostBuilder = Config.subCategoryGadgets.subCategoryGadgetBuilding.energyCostBuilder;
        durabilityBuilder = Config.subCategoryGadgets.subCategoryGadgetBuilding.durabilityBuilder;

        energyCostExchanger = Config.subCategoryGadgets.subCategoryGadgetExchanger.energyCostExchanger;
        durabilityExchanger = Config.subCategoryGadgets.subCategoryGadgetExchanger.durabilityExchanger;

        energyMaxDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.energyMaxDestruction;
        energyCostDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.energyCostDestruction;
        durabilityDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.durabilityDestruction;

        energyCostCopyPaste = Config.subCategoryGadgets.subCategoryGadgetCopyPaste.energyCostCopyPaste;
        durabilityCopyPaste = Config.subCategoryGadgets.subCategoryGadgetCopyPaste.durabilityCopyPaste;
        handleSpecialCases();
    }

    private static void handleSpecialCases() {
        BlacklistBlocks.parseBlackList(blockBlacklist);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            BuildingGadgets.logger.info("Sending InGameConfig to freshly logged in client.");
            PacketHandler.INSTANCE.sendTo(new PacketSyncConfig(parseSynchronisation()), (EntityPlayerMP) event.player);
        }
    }

    private static NBTTagCompound parseSynchronisation() {
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
        List<Field> fields = getSyncFields();
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
        handleSpecialCases();
    }

    private static NBTTagCompound parseField(Field field) {
        NBTBase valueTag = FieldSerializer.parseFieldValue(field);
        if (valueTag==null) {
            BuildingGadgets.logger.warn("Could not use type of Field "+field.getName()+"!"+" Found type "+field.getType().getName()+"!");
            return null;
        }
        String name = getSyncName(field);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(KEY_VALUE,valueTag);
        compound.setString(KEY_NAME,name);
        return compound;
    }

    private static void handleNBT(NBTTagCompound compound, Map<String,Field> fields) {
        if (!compound.hasKey(KEY_NAME) || !compound.hasKey(KEY_VALUE)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an inproperly initialised NBTTagCompound!");
            return;
        }
        String name = compound.getString(KEY_NAME);
        NBTBase rawValue = compound.getTag(KEY_VALUE);
        if (!fields.containsKey(name)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an unknown Field!");
            return;
        }
        FieldSerializer.applyValue(rawValue,fields.get(name));
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

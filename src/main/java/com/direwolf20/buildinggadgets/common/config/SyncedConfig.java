package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.fieldmap.FieldMapper;
import com.direwolf20.buildinggadgets.common.config.fieldmap.FieldSerializer;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketSyncConfig;
import com.direwolf20.buildinggadgets.common.tools.ReflectionTool;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container class representing the actual In-Game Config Values, which are synced to the Client.
 * All Fields marked with {@link AutoSync} will be automatically synced to the Client.
 * Just omit the Annotation for an Client-only Config value.
 * @see AutoSync for more Information
 */
public class SyncedConfig {
    private static final String KEY_NAME = "NAME";
    private static final String KEY_VALUE = "VAL";

    @AutoSync
    public static int energyCostBuilder;

    @AutoSync
    public static int energyCostExchanger;

    @AutoSync
    public static int energyCostDestruction;

    @AutoSync
    public static int energyCostCopyPaste;

    @AutoSync
    public static int damageCostBuilder;

    @AutoSync
    public static int damageCostExchanger;

    @AutoSync
    public static int damageCostDestruction;

    @AutoSync
    public static int damageCostCopyPaste;

    @AutoSync
    public static int energyMax;

    @AutoSync
    public static int energyMaxDestruction;

    @AutoSync
    public static boolean poweredByFE;

    @AutoSync
    public static int durabilityBuilder;

    @AutoSync
    public static int durabilityExchanger;

    @AutoSync
    public static int durabilityDestruction;

    @AutoSync
    public static double nonFuzzyMultiplierDestruction;

    @AutoSync
    public static boolean nonFuzzyEnabledDestruction;

    @AutoSync
    public static int durabilityCopyPaste;

    @AutoSync
    public static double rayTraceRange;

    @AutoSync
    public static int maxRange;

    @AutoSync
    public static boolean enableDestructionGadget;

    @AutoSync
    public static boolean absoluteCoordDefault;

    @AutoSync
    public static boolean canOverwriteBlocks;

    @AutoSync
    public static boolean enablePaste;

    @AutoSync
    public static int t1ContainerCapacity;

    @AutoSync
    public static int t2ContainerCapacity;

    @AutoSync
    public static int t3ContainerCapacity;

    @AutoSync(mapperId = FieldMapper.PATTERN_LIST_MAPPER_ID)
    public static PatternList blockBlacklist;

    /**
     * This Methods transfers Data from the 'Master copy' (the normal {@link Config}) into the SyncedConfig,
     * so that it can be synced to the Client.
     */
    static void transferValues() {
        rayTraceRange = Config.rayTraceRange;
        poweredByFE = Config.poweredByFE;
        enableDestructionGadget = Config.enableDestructionGadget;
        absoluteCoordDefault = Config.absoluteCoordDefault;
        canOverwriteBlocks = Config.canOverwriteBlocks;
        enablePaste = Config.enablePaste;

        blockBlacklist = FieldMapper.PATTERN_LIST_MAPPER.mapToField(Config.subCategoryBlacklist.blockBlacklist);

        maxRange = Config.subCategoryGadgets.maxRange;
        energyMax = Config.subCategoryGadgets.maxEnergy;

        energyCostBuilder = Config.subCategoryGadgets.subCategoryGadgetBuilding.energyCostBuilder;
        damageCostBuilder = Config.subCategoryGadgets.subCategoryGadgetBuilding.damageCostBuilder;
        durabilityBuilder = Config.subCategoryGadgets.subCategoryGadgetBuilding.durabilityBuilder;

        energyCostExchanger = Config.subCategoryGadgets.subCategoryGadgetExchanger.energyCostExchanger;
        damageCostExchanger = Config.subCategoryGadgets.subCategoryGadgetExchanger.damageCostExchanger;
        durabilityExchanger = Config.subCategoryGadgets.subCategoryGadgetExchanger.durabilityExchanger;

        energyMaxDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.energyMaxDestruction;
        energyCostDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.energyCostDestruction;
        damageCostDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.damageCostDestruction;
        durabilityDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.durabilityDestruction;
        nonFuzzyMultiplierDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.nonFuzzyMultiplier;
        nonFuzzyEnabledDestruction = Config.subCategoryGadgets.subCategoryGadgetDestruction.nonFuzzyEnabled;

        energyCostCopyPaste = Config.subCategoryGadgets.subCategoryGadgetCopyPaste.energyCostCopyPaste;
        damageCostCopyPaste = Config.subCategoryGadgets.subCategoryGadgetCopyPaste.damageCostCopyPaste;
        durabilityCopyPaste = Config.subCategoryGadgets.subCategoryGadgetCopyPaste.durabilityCopyPaste;

        t1ContainerCapacity = Config.subCategoryPasteContainers.t1Capacity;
        t2ContainerCapacity = Config.subCategoryPasteContainers.t2Capacity;
        t3ContainerCapacity = Config.subCategoryPasteContainers.t3Capacity;
    }

    /**
     *
     * @param player The player who's Config needs to be updated with this Server's Config Data
     * @implSpec Will do nothing if called from outside game or from the Wrong Side
     */
    public static void sendConfigUpdateTo(EntityPlayerMP player) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            //Testing showed, that this does absolutely nothing if called from outside game (besides taking up a small bit of processing time of course)
            PacketHandler.INSTANCE.sendTo(new PacketSyncConfig(SyncedConfig.parseSynchronisation()), player);
    }

    private static NBTTagCompound parseSynchronisation() {
        //update Data for the case that the config changed
        transferValues();
        NBTTagList list = new NBTTagList();
        List<Field> fields = Collections.unmodifiableList(getSyncFields());
        for (Field field : fields) {
            NBTTagCompound compound = parseField(field);
            if (compound != null) list.appendTag(compound);
        }
        NBTTagCompound compound = new NBTTagCompound(); //ByteBufUtil requires TagCompounds...
        compound.setTag(KEY_VALUE,list);
        return compound;
    }

    /**
     * Reads the config Data from the given {@link NBTTagCompound}.
     * @param compound The compound to use for updating synced config Data
     */
    public static void onReadSynchronisation(NBTTagCompound compound) {
        //update Data for potentially existing Client only Config Values
        transferValues();
        if (!compound.hasKey(KEY_VALUE))
            return;
        NBTTagList list = (NBTTagList) compound.getTag(KEY_VALUE);
        List<Field> fields = getSyncFields();
        Map<String, Field> map = new HashMap<>();
        for (Field f : fields) {
            map.put(getSyncName(f), f);
        }
        for (NBTBase rawNBT : list) {
            if (rawNBT instanceof NBTTagCompound) {
                handleNBT((NBTTagCompound) rawNBT, map);
            } else {
                BuildingGadgets.logger.warn("Unexpected " + rawNBT.getClass().getName() + " found in NBTTagList which was expected to only contain NBTTagCompounds!");
            }
        }
    }

    private static String getMapperIdFor(Field f) {
        return f.getAnnotation(AutoSync.class).mapperId();
    }

    private static NBTTagCompound parseField(Field field) {
        NBTBase valueTag = FieldSerializer.parseFieldValue(field,getMapperIdFor(field));
        if (valueTag == null) {
            BuildingGadgets.logger.warn("Could not use type of Field " + field.getName() + "!" + " Found type " + field.getType().getName() + "!");
            return null;
        }
        String name = getSyncName(field);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(KEY_VALUE,valueTag);
        compound.setString(KEY_NAME,name);
        return compound;
    }

    private static void handleNBT(NBTTagCompound compound, Map<String, Field> fields) {
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
        Field field = fields.get(name);
        FieldSerializer.applyValue(rawValue, field,getMapperIdFor(field));
    }

    private static List<Field> getSyncFields() {
        return ReflectionTool.getFilteredFields(SyncedConfig.class, field -> field.isAnnotationPresent(AutoSync.class) && ReflectionTool.PREDICATE_STATIC.test(field));
    }

    private static String getSyncName(Field field) {
        String name = field.getAnnotation(AutoSync.class).value();
        return name.isEmpty() ? field.getName() : name;
    }

    static {
        transferValues();//ensure that everything is initialised after the class was loaded
    }
}

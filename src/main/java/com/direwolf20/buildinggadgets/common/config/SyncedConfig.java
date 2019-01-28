package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.fieldmap.FieldMapper;
import com.direwolf20.buildinggadgets.common.config.fieldmap.FieldSerializer;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketSyncConfig;
import com.direwolf20.buildinggadgets.common.tools.ReflectionTool;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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

    @AutoSync(mapperId = FieldMapper.PATTERN_LIST_MAPPER_ID)
    public static PatternList blockBlacklist;

    /**
     * This Methods transfers Data from the 'Master copy' (the normal {@link Config}) into the SyncedConfig,
     * so that it can be synced to the Client.
     */
    static void transferValues() {
        rayTraceRange = Config.GENERAL.rayTraceRange.get();
        poweredByFE = Config.GENERAL.poweredByFE.get();
        enableDestructionGadget = Config.GENERAL.enableDestructionGadget.get();
        absoluteCoordDefault = Config.GENERAL.absoluteCoordDefault.get();
        canOverwriteBlocks = Config.GENERAL.canOverwriteBlocks.get();
        enablePaste = Config.GENERAL.enablePaste.get();

        blockBlacklist = FieldMapper.PATTERN_LIST_MAPPER.mapToField(Config.BLACKLIST.blockBlacklist.get());

        maxRange = Config.GADGETS.maxRange.get();
        energyMax = Config.GADGETS.maxEnergy.get();

        energyCostBuilder = Config.GADGETS.subCategoryGadgetBuilding.energyCost.get();
        durabilityBuilder = Config.GADGETS.subCategoryGadgetBuilding.durability.get();

        energyCostExchanger = Config.GADGETS.subCategoryGadgetExchanger.energyCost.get();
        durabilityExchanger = Config.GADGETS.subCategoryGadgetExchanger.durability.get();

        energyMaxDestruction = Config.GADGETS.subCategoryGadgetDestruction.energyMax.get();
        energyCostDestruction = Config.GADGETS.subCategoryGadgetDestruction.energyCost.get();
        durabilityDestruction = Config.GADGETS.subCategoryGadgetDestruction.durability.get();

        energyCostCopyPaste = Config.GADGETS.subCategoryGadgetCopyPaste.energyCost.get();
        durabilityCopyPaste = Config.GADGETS.subCategoryGadgetCopyPaste.durability.get();
    }

    /**
     *
     * @param player The player who's Config needs to be updated with this Server's Config Data
     * @implSpec Will do nothing if called from outside game or from the Wrong Side
     */
    public static void sendConfigUpdateTo(EntityPlayerMP player) {
        //TODO Networking
        //Testing showed, that this does absolutely nothing if called from outside game (besides taking up a small bit of processing time of course)
        PacketHandler.sendTo(new PacketSyncConfig(SyncedConfig.parseSynchronisation()), player);
    }

    private static NBTTagCompound parseSynchronisation() {
        //update Data for the case that the config changed
        transferValues();
        NBTTagList list = new NBTTagList();
        List<Field> fields = Collections.unmodifiableList(getSyncFields());
        for (Field field: fields) {
            NBTTagCompound compound = parseField(field);
            if (compound != null) list.add(compound);
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
        Map<String,Field> map = new HashMap<>();
        for (Field f: fields) {
            map.put(getSyncName(f),f);
        }
        for (INBTBase rawNBT : list) {
            if (rawNBT instanceof NBTTagCompound) {
                handleNBT((NBTTagCompound) rawNBT,map);
            } else {
                BuildingGadgets.logger.warn("Unexpected "+rawNBT.getClass().getName()+" found in NBTTagList which was expected to only contain NBTTagCompounds!");
            }
        }
    }

    private static String getMapperIdFor(Field f) {
        return f.getAnnotation(AutoSync.class).mapperId();
    }

    private static NBTTagCompound parseField(Field field) {
        INBTBase valueTag = FieldSerializer.parseFieldValue(field, getMapperIdFor(field));
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
        INBTBase rawValue = compound.getTag(KEY_VALUE);
        if (!fields.containsKey(name)) {
            BuildingGadgets.logger.warn("Tried to read synchronisation from an unknown Field!");
            return;
        }
        Field field = fields.get(name);
        FieldSerializer.applyValue(rawValue,field,getMapperIdFor(field));
    }

    private static List<Field> getSyncFields() {
        return ReflectionTool.getFilteredFields(SyncedConfig.class, field -> field.isAnnotationPresent(AutoSync.class) && ReflectionTool.PREDICATE_STATIC.test(field));
    }

    private static String getSyncName(Field field) {
        String name = field.getAnnotation(AutoSync.class).value();
        return name.isEmpty()?field.getName():name;
    }

    static {
        transferValues();//ensure that everything is initialised after the class was loaded
    }
}

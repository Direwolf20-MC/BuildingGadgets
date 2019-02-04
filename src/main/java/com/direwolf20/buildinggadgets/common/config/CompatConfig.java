package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class CompatConfig {
    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_BLACKLIST = "blockBlacklist";

    private static int maxRange = 15;
    private static int energyCostBuilder = 50;
    private static int energyCostExchanger = 100;
    private static int energyCostDestruction = 200;
    private static int energyMax = 500000;
    private static int energyMaxDestruction = 1000000;
    private static boolean poweredByFE = true;
    private static int durabilityBuilder = 500;
    private static int durabilityExchanger = 500;
    private static int durabilityDestruction = 500;
    private static int durabilityCopyPaste = 500;
    private static boolean enablePaste = true;
    private static boolean enableDestructionTool = true;
    private static boolean absoluteCoordDefault = false;
    private static boolean canOverwriteBlocks = true;
    private static String[] blacklist = {};

    public static boolean readConfig(File compatConfigFile) {
        if (compatConfigFile == null || !compatConfigFile.exists())
            return false;
        Configuration cfg = new Configuration(compatConfigFile);
        try {
            parseCompatConfig(cfg);
            compatConfigFile.delete();
            return true;
        } catch (Exception e1) {
            BuildingGadgets.logger.error("Error loading compat-config file! ", e1);
        }
        return false;
    }

    private static void parseCompatConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
        maxRange = cfg.getInt("maxRange", CATEGORY_GENERAL, maxRange, 1, 25, "The max range of the building tool");
        energyCostBuilder = cfg.getInt("energyCostBuilder", CATEGORY_GENERAL, energyCostBuilder, 0, 100000, "The energy cost of the Builder per block");
        energyCostExchanger = cfg.getInt("energyCostExchanger", CATEGORY_GENERAL, energyCostExchanger, 0, 100000, "The energy cost of the Exchanger per block");
        energyCostDestruction = cfg.getInt("energyCostDestruction", CATEGORY_GENERAL, energyCostDestruction, 0, 100000, "The energy cost of the Destruction Gadget per block");
        energyMax = cfg.getInt("energyMax", CATEGORY_GENERAL, energyMax, 0, Integer.MAX_VALUE, "The max energy of the Builder & Exchanger");
        energyMaxDestruction = cfg.getInt("energyMaxDestruction", CATEGORY_GENERAL, energyMaxDestruction, 0, Integer.MAX_VALUE, "The max energy of the Destruction Gadget");
        poweredByFE = cfg.getBoolean("poweredByFE", CATEGORY_GENERAL, poweredByFE, "Set to true for Forge Energy Support, set to False for vanilla Item Damage");
        durabilityBuilder = cfg.getInt("durabilityBuilder", CATEGORY_GENERAL, durabilityBuilder, 0, 100000, "The max durability of the Builder (Ignored if powered by FE)");
        durabilityExchanger = cfg.getInt("durabilityExchanger", CATEGORY_GENERAL, durabilityExchanger, 0, 100000, "The max durability of the Exchanger (Ignored if powered by FE)");
        durabilityDestruction = cfg.getInt("durabilityDestruction", CATEGORY_GENERAL, durabilityDestruction, 0, 100000, "The max durability of the Destruction Gadget (Ignored if powered by FE)");
        durabilityCopyPaste = cfg.getInt("durabilityCopyPaste", CATEGORY_GENERAL, durabilityCopyPaste, 0, 100000, "The max durability of the Copy & Paste Gadget (Ignored if powered by FE)");
        enablePaste = cfg.getBoolean("enablePaste", CATEGORY_GENERAL, enablePaste, "Set to false to disable the recipe for construction paste.");
        enableDestructionTool = cfg.getBoolean("enableDestructionTool", CATEGORY_GENERAL, enableDestructionTool, "Set to false to disable the destruction tool.");
        absoluteCoordDefault = cfg.getBoolean("absoluteCoordinateModeDefault", CATEGORY_GENERAL, absoluteCoordDefault, "Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default. Set to true for Absolute, set to False for Relative.");
        canOverwriteBlocks = cfg.getBoolean("canOverwriteBlocks", CATEGORY_GENERAL, canOverwriteBlocks, "Whether the Builder / CopyPaste gadgets can overwrite blocks like water, lava, grass, etc (like a player can). False will only allow it to overwrite air blocks.");
        cfg.addCustomCategoryComment(CATEGORY_BLACKLIST, "Blacklist configuration");
        ConfigCategory category = cfg.getCategory(CATEGORY_BLACKLIST);
        if (!category.isEmpty())
            blacklist = category.get("Blacklist").getStringList();
    }

    public static void applyCompatConfig() {
        Config.enablePaste = enablePaste;
        Config.enableDestructionGadget = enableDestructionTool;
        Config.absoluteCoordDefault = absoluteCoordDefault;
        Config.canOverwriteBlocks = canOverwriteBlocks;
        Config.poweredByFE = poweredByFE;
        Config.subCategoryBlacklist.blockBlacklist = blacklist;
        Config.subCategoryGadgets.maxEnergy = energyMax;
        Config.subCategoryGadgets.maxRange = maxRange;
        Config.subCategoryGadgets.subCategoryGadgetBuilding.energyCostBuilder = energyCostBuilder;
        Config.subCategoryGadgets.subCategoryGadgetBuilding.durabilityBuilder = durabilityBuilder;
        Config.subCategoryGadgets.subCategoryGadgetExchanger.durabilityExchanger = durabilityExchanger;
        Config.subCategoryGadgets.subCategoryGadgetExchanger.energyCostExchanger = energyCostExchanger;
        Config.subCategoryGadgets.subCategoryGadgetDestruction.durabilityDestruction = durabilityDestruction;
        Config.subCategoryGadgets.subCategoryGadgetDestruction.energyCostDestruction = energyCostDestruction;
        Config.subCategoryGadgets.subCategoryGadgetDestruction.energyMaxDestruction = energyMaxDestruction;
        Config.subCategoryGadgets.subCategoryGadgetCopyPaste.durabilityCopyPaste = durabilityCopyPaste;
        //we are definitely not in a Game - no need to parse and write the Sync...
        SyncedConfig.transferValues();
        //save the values to the new config
        ConfigManager.sync(BuildingGadgets.MODID, Type.INSTANCE);
    }
}

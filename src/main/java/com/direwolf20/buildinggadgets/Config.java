package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.tools.BlacklistBlocks;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class Config {
    private static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_BLACKLIST = "blockBlacklist";
    //

    // This values below you can access elsewhere in your mod:
    public static int maxRange = 15;
    public static int energyCostBuilder = 50;
    public static int energyCostExchanger = 100;
    public static int energyMax = 500000;
    public static boolean poweredByFE = true;
    public static int durabilityBuilder = 500;
    public static int durabilityExchanger = 500;
    public static boolean enablePaste = true;
    public static boolean absoluteCoordDefault = false;

    // Call this from CommonProxy.preInit(). It will create our config if it doesn't
    // exist yet and read the values if it does exist.
    public static void readConfig() {
        Configuration cfg = CommonProxy.config;
        try {
            cfg.load();
            initGeneralConfig(cfg);
            //initDimensionConfig(cfg);
        } catch (Exception e1) {
            BuildingGadgets.logger.log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    private static void initGeneralConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
        // cfg.getBoolean() will get the value in the config if it is already specified there. If not it will create the value.
        maxRange = cfg.getInt("maxRange", CATEGORY_GENERAL, maxRange, 1, 25, "The max range of the building tool");
        energyCostBuilder = cfg.getInt("energyCostBuilder", CATEGORY_GENERAL, energyCostBuilder, 0, 100000, "The energy cost of the Builder per block");
        energyCostExchanger = cfg.getInt("energyCostExchanger", CATEGORY_GENERAL, energyCostExchanger, 0, 100000, "The energy cost of the Exchanger per block");
        energyMax = cfg.getInt("energyMax", CATEGORY_GENERAL, energyMax, 0, Integer.MAX_VALUE, "The max energy of the Builder & Exchanger");
        poweredByFE = cfg.getBoolean("poweredByFE", CATEGORY_GENERAL, poweredByFE, "Set to true for Forge Energy Support, set to False for vanilla Item Damage");
        durabilityBuilder = cfg.getInt("durabilityBuilder", CATEGORY_GENERAL, durabilityBuilder, 0, 100000, "The max durability of the Builder (Ignored if powered by FE)");
        durabilityExchanger = cfg.getInt("durabilityExchanger", CATEGORY_GENERAL, durabilityExchanger, 0, 100000, "The max durability of the Exchanger (Ignored if powered by FE)");
        enablePaste = cfg.getBoolean("enablePaste", CATEGORY_GENERAL, enablePaste, "Set to false to disable the recipe for construction paste.");
        absoluteCoordDefault = cfg.getBoolean("absoluteCoordinateModeDefault", CATEGORY_GENERAL, absoluteCoordDefault, "Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default. Set to true for Absolute, set to False for Relative.");
        cfg.addCustomCategoryComment(CATEGORY_BLACKLIST, "Blacklist configuration");
        BlacklistBlocks.getBlacklist(cfg);
    }

}

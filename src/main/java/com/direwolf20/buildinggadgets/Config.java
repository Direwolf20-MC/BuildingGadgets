package com.direwolf20.buildinggadgets;

import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

public class Config {
    private static final String CATEGORY_GENERAL = "general";
    //

    // This values below you can access elsewhere in your mod:
    public static int maxRange = 15;
    public static int energyCostBuilder = 50;
    public static int energyCostExchanger = 100;
    public static int energyMax = 500000;
    public static boolean poweredByFE = true;

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
        poweredByFE = cfg.getBoolean("poweredByFE", CATEGORY_GENERAL, poweredByFE,"Set to true for Forge Energy Support, set to False for vanilla Item Damage");
    }

}

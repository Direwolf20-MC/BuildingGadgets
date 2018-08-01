package com.direwolf20.buildinggadgets.config;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraftforge.common.config.Config.*;

@net.minecraftforge.common.config.Config(modid = BuildingGadgets.MODID, name = BuildingGadgets.MODNAME, category =  Config.CATEGORY_ROOT)
public class Config {
    @Ignore
    static final String CATEGORY_ROOT = "general";

    @Comment("Defines how far away you can build")
    @RangeDouble(min=1,max=48)
    @Name("Trace Distance")
    public static double rayTraceRange = 32;

    @Comment("Set to true for Forge Energy Support, set to False for vanilla Item Damage")
    public static boolean poweredByFE = true;

    @Name("Gadgets")
    public static CategoryGadgets subCategoryGadgets = new CategoryGadgets();

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
        private CategoryGadgets() { }

        @Comment("The max range of the building tool")
        @RangeInt(min = 1, max = 25)
        public int maxRange = 15;

        @Comment("The max energy of the Builder & Exchanger")
        @RangeInt(min = 0)
        public int maxEnergy = 500000;

        @Comment("Enables the player to set Anchors for both Gadgets")
        @Name("Allow Anchors")
        public boolean allowAnchors = true;
        @Name("Building Gadget")
        public CategoryBuildingGadget subCategoryBuildingGadget = new CategoryBuildingGadget();
        @Name("Exchanging Gadget")
        public CategoryExchangerTool subCategoryExchanger = new CategoryExchangerTool();

        public static final class CategoryBuildingGadget {
            private CategoryBuildingGadget(){ }
            @Comment("The energy cost of the Builder per block")
            @RangeInt(min = 0, max = 100000)
            public int energyCostBuilder = 50;
            @Comment("The max durability of the Builder (Ignored if powered by FE)")
            @RangeInt(min = 0, max = 100000)
            public int durabilityBuilder = 500;
            @Name("Building Gadget Mode-WhiteList")
            public String[] modeWhiteList = {};
            @Name("Building Gadget Mode-BlackList")
            public String[] modeBlackList = {};
            @Name("Set Block Whitelist")
            public String[] setBlockWhiteList = {};
            @Name("Set Block Blacklist")
            public String[] setBlockBlackList = {};
        }

        public static final class CategoryExchangerTool {
            private CategoryExchangerTool(){ }
            @Comment("The energy cost of the Exchanger per block")
            @RangeInt(min = 0, max = 100000)
            public int energyCostExchanger = 100;
            @Comment("The max durability of the Exchanger (Ignored if powered by FE)")
            @RangeInt(min = 0, max = 100000)
            public int durabilityExchanger = 500;
            @Name("Exchanging Gadget Mode-WhiteList")
            public String[] modeWhiteList = {};
            @Name("Exchanging Gadget Mode-BlackList")
            public String[] modeBlackList = {};
            @Name("Exchange Block Whitelist")
            public String[] exchangeBlockWhiteList = {};
            @Name("Exchange Block Blacklist")
            public String[] exchangeBlockBlackList = {};
            @Name("Changable Block Whitelist")
            public String[] changeBlockWhiteList = {};
            @Name("Changable Block Blacklist")
            public String[] changeBlockBlackList = {};
        }
    }


}

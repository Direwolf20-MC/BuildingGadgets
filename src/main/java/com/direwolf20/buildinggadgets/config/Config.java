package com.direwolf20.buildinggadgets.config;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraftforge.common.config.Config.*;

@net.minecraftforge.common.config.Config(modid = BuildingGadgets.MODID, name = BuildingGadgets.MODNAME, category =  Config.CATEGORY_ROOT)
public class Config {
    @Ignore
    static final String CATEGORY_ROOT = "general";

    @Comment("Defines how far away you can build")
    @RangeDouble(min=1,max=128)
    @Name("Trace Distance")
    public static double rayTraceRange = 32;

    @Name("Gadgets")
    public static CategoryGadgets subCategoryGadgets = new CategoryGadgets();

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
        private CategoryGadgets() { }

        @Comment(
                {"Defines the Building Gadgets max building range",
                 "For example to range of the Vertical column mode"})
        @RangeInt(min = 1, max = 128)
        @Name("Gadget Max Range")
        public int maxRange = 15;

        @Comment("Enables the player to set Anchors for both Gadgets")
        @Name("Allow Anchors")
        public boolean allowAnchors = true;
        @Name("Building Gadget")
        public CategoryBuildingGadget subCategoryBuildingGadget = new CategoryBuildingGadget();
        @Name("Exchanging Gadget")
        public CategoryExchangerTool subCategoryExchanger = new CategoryExchangerTool();

        public static final class CategoryBuildingGadget {
            private CategoryBuildingGadget(){ }
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

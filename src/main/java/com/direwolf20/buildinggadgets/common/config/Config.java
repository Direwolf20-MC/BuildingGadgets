package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Config.*;

//hardcode this name, so that we don't use a different config file than before we used Annotations
@net.minecraftforge.common.config.Config(modid = BuildingGadgets.MODID, name = "Building Gadgets", category =  Config.CATEGORY_ROOT)
public class Config {

    static final String CATEGORY_ROOT = "general";

    private static final String LANG_KEY_ROOT = BuildingGadgets.MODID +".config."+CATEGORY_ROOT;

    private static final String LANG_KEY_BLACKLIST = LANG_KEY_ROOT+".subCategoryBlacklist";

    private static final String LANG_KEY_GADGETS = LANG_KEY_ROOT+".subCategoryGadgets";

    private static final String LANG_KEY_GADGET_BUILDING = LANG_KEY_GADGETS+".gadgetBuilding";

    private static final String LANG_KEY_GADGET_EXCHANGER = LANG_KEY_GADGETS+".gadgetExchanger";

    private static final String LANG_KEY_GADGET_DESTRUCTION = LANG_KEY_GADGETS+".gadgetDestruction";

    private static final String LANG_KEY_GADGET_COPY_PASTE = LANG_KEY_GADGETS+".gadgetCopyPaste";

    @RangeDouble(min=1,max=48)
    @Name("Max Build Distance")
    @Comment("Defines how far away you can build")
    @LangKey(LANG_KEY_ROOT+".rayTraceRange")
    public static double rayTraceRange = 32;

    @Name("Powered by Forge Energy")
    @Comment("Set to true for Forge Energy Support, set to False for vanilla Item Damage")
    @LangKey(LANG_KEY_ROOT+".poweredByFE")
    public static boolean poweredByFE = true;

    @RequiresMcRestart
    @Name("Enable Construction Paste")
    @Comment("Set to false to disable the recipe for construction paste.")
    @LangKey(LANG_KEY_ROOT+".enablePaste")
    public static boolean enablePaste = true;

    @RequiresMcRestart
    @Name("Enable Destruction Gadget")
    @Comment("Set to false to disable the Destruction Gadget.")
    @LangKey(LANG_KEY_ROOT+".enableDestructionGadget")
    public static boolean enableDestructionGadget = true;

    @Name("Default to absolute Coord-Mode")
    @Comment({"Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.",
              "Set to true for Absolute, set to False for Relative."})
    @LangKey(LANG_KEY_ROOT+".absoluteCoordDefault")
    public static boolean absoluteCoordDefault = false;

    @Name("Allow non-Air-Block-Overwrite")
    @Comment({"Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).",
              "False will only allow it to overwrite air blocks."})
    @LangKey(LANG_KEY_ROOT+".canOverwriteBlocks")
    public static boolean canOverwriteBlocks = true;

    @Name("Blacklist Settings")
    @Comment("Configure your Blacklist-Settings here")
    @LangKey(LANG_KEY_BLACKLIST)
    public static CategoryBlacklist subCategoryBlacklist = new CategoryBlacklist();

    public static final class CategoryBlacklist {
        //In 1.13 this should be converted to a tag (or at least made compatible with)
        @Name("Blacklisted Blocks")
        @Comment({"All Blocks added to this will be treated similar to TileEntities. Not at all.",
                  "Notice that you can use Regular Expressions as defined by Java Patterns to express more complex name combinations.",
                  "Use for example \"awfulmod:.*\" to blacklist all awfulmod Blocks."})
        @LangKey(LANG_KEY_BLACKLIST+".blockBlacklist")
        public String[] blockBlacklist = {"minecraft:.*door.*",PatternList.getName(Blocks.PISTON_HEAD)};
    }

    @Name("Gadgets")
    @Comment("Configure the Gadgets here")
    @LangKey(LANG_KEY_GADGETS)
    public static CategoryGadgets subCategoryGadgets = new CategoryGadgets();

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
        private CategoryGadgets() { }

        @RangeInt(min = 1, max = 25)
        @Name("Maximum allowed Range")
        @Comment("The max range of the Gadgets")
        @LangKey(LANG_KEY_GADGETS+".maxRange")
        public int maxRange = 15;

        @RangeInt(min = 0)
        @Name("Maximum Energy")
        @Comment("The max energy of Building, Exchanging & Copy-Paste Gadget")
        @LangKey(LANG_KEY_GADGETS+".maxEnergy")
        public int maxEnergy = 500000;

        @Name("Building Gadget")
        @Comment("Energy Cost & Durability of the Building Gadget")
        @LangKey(LANG_KEY_GADGET_BUILDING)
        public CategoryGadgetBuilding subCategoryGadgetBuilding = new CategoryGadgetBuilding();

        @Name("Exchanging Gadget")
        @Comment("Energy Cost & Durability of the Exchanging Gadget")
        @LangKey(LANG_KEY_GADGET_EXCHANGER)
        public CategoryGadgetExchanger subCategoryGadgetExchanger = new CategoryGadgetExchanger();

        @Name("Destruction Gadget")
        @Comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")
        @LangKey(LANG_KEY_GADGET_DESTRUCTION)
        public CategoryGadgetDestruction subCategoryGadgetDestruction = new CategoryGadgetDestruction();

        @Name("Copy-Paste Gadget")
        @Comment("Energy Cost & Durability of the Copy-Paste Gadget")
        @LangKey(LANG_KEY_GADGET_COPY_PASTE)
        public CategoryGadgetCopyPaste subCategoryGadgetCopyPaste = new CategoryGadgetCopyPaste();

        public static final class CategoryGadgetBuilding {
            private CategoryGadgetBuilding(){ }

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment("The energy cost of the Builder per block")
            @LangKey(LANG_KEY_GADGETS+".energyCost")
            public int energyCostBuilder = 50;

            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment("The max durability of the Builder (Ignored if powered by FE)")
            @LangKey(LANG_KEY_GADGETS+".durability")
            public int durabilityBuilder = 500;
        }

        public static final class CategoryGadgetExchanger {
            private CategoryGadgetExchanger(){ }

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment("The energy cost of the Exchanger per block")
            @LangKey(LANG_KEY_GADGETS+".energyCost")
            public int energyCostExchanger = 100;

            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment("The max durability of the Exchanger (Ignored if powered by FE)")
            @LangKey(LANG_KEY_GADGETS+".durability")
            public int durabilityExchanger = 500;
        }

        public static final class CategoryGadgetDestruction {
            private CategoryGadgetDestruction () {}
            @RangeInt(min = 0)
            @Name("Maximum Energy")
            @Comment("The max energy of the Destruction Gadget")
            @LangKey(LANG_KEY_GADGET_DESTRUCTION+".maxEnergy")
            public int energyMaxDestruction = 1000000;

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment("The energy cost of the Destruction Gadget per block")
            @LangKey(LANG_KEY_GADGETS+".energyCost")
            public int energyCostDestruction = 200;

            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment("The max durability of the Destruction Gadget (Ignored if powered by FE)")
            @LangKey(LANG_KEY_GADGETS+".durability")
            public int durabilityDestruction = 500;
        }

        public static final class CategoryGadgetCopyPaste {
            private CategoryGadgetCopyPaste() { }

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment("The Energy Use of the Copy Paste Gadget")
            @LangKey(LANG_KEY_GADGETS+".energyCost")
            public int energyCostCopyPaste = 50;

            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment("The max durability of the Copy & Paste Gadget (Ignored if powered by FE)")
            @LangKey(LANG_KEY_GADGETS+".durability")
            public int durabilityCopyPaste = 500;
        }
    }


}

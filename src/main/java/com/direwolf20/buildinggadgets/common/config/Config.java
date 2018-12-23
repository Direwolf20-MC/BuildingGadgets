package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Config.*;

import static com.direwolf20.buildinggadgets.common.config.BlacklistBlocks.getName;

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

    @Comment("Set to false to disable the recipe for construction paste.")
    public static boolean enablePaste = true;
    @Comment("Set to false to disable the Destruction Gadget.")
    public static boolean enableDestructionGadget = true;
    @Comment({"Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.",
              "Set to true for Absolute, set to False for Relative."})
    public static boolean absoluteCoordDefault = false;
    @Comment({"Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).",
              "False will only allow it to overwrite air blocks."})
    public static boolean canOverwriteBlocks = true;

    @Name("Blacklist configuration")
    public static CategoryBlacklist subCategoryBlacklist = new CategoryBlacklist();

    public static final class CategoryBlacklist {
        //In 1.13 this should be converted to a tag
        @Name("Blacklist")
        public String[] blockBlacklist = {getName(Blocks.OAK_DOOR),getName(Blocks.BIRCH_DOOR),getName(Blocks.ACACIA_DOOR),getName(Blocks.DARK_OAK_DOOR),getName(Blocks.IRON_DOOR),getName(Blocks.JUNGLE_DOOR),getName(Blocks.SPRUCE_DOOR),getName(Blocks.PISTON_HEAD)};
    }

    @Name("Gadgets")
    public static CategoryGadgets subCategoryGadgets = new CategoryGadgets();

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
        private CategoryGadgets() { }

        @Comment("The max range of the Building Gadget")
        @RangeInt(min = 1, max = 25)
        public int maxRange = 15;

        @Comment("The max energy of Building, Exchanging & Copy-Paste Gadget")
        @RangeInt(min = 0)
        public int maxEnergy = 500000;

        @Name("Building Gadget")
        public CategoryGadgetBuilding subCategoryGadgetBuilding = new CategoryGadgetBuilding();
        @Name("Exchanging Gadget")
        public CategoryGadgetExchanger subCategoryGadgetExchanger = new CategoryGadgetExchanger();
        @Name("Destruction Gadget")
        public CategoryGadgetDestruction subCategoryGadgetDestruction = new CategoryGadgetDestruction();
        @Name("Copy & Paste Gadget")
        public CategoryGadgetCopyPaste subCategoryGadgetCopyPaste = new CategoryGadgetCopyPaste();

        public static final class CategoryGadgetBuilding {
            private CategoryGadgetBuilding(){ }
            @Comment("The energy cost of the Builder per block")
            @RangeInt(min = 0, max = 100000)
            public int energyCostBuilder = 50;
            @Comment("The max durability of the Builder (Ignored if powered by FE)")
            @RangeInt(min = 0, max = 100000)
            public int durabilityBuilder = 500;
        }

        public static final class CategoryGadgetExchanger {
            private CategoryGadgetExchanger(){ }
            @Comment("The energy cost of the Exchanger per block")
            @RangeInt(min = 0, max = 100000)
            public int energyCostExchanger = 100;
            @Comment("The max durability of the Exchanger (Ignored if powered by FE)")
            @RangeInt(min = 0, max = 100000)
            public int durabilityExchanger = 500;
        }

        public static final class CategoryGadgetDestruction {
            @Comment("The max energy of the Destruction Gadget")
            @RangeInt(min = 0)
            public int energyMaxDestruction = 1000000;
            @Comment("The energy cost of the Destruction Gadget per block")
            @RangeInt(min = 0, max = 100000)
            public int energyCostDestruction = 200;
            @Comment("The max durability of the Destruction Gadget (Ignored if powered by FE)")
            @RangeInt(min = 0, max = 100000)
            public int durabilityDestruction = 500;
        }

        public static final class CategoryGadgetCopyPaste {
            @Comment("The Energy Use of the Copy Paste Gadget")
            @RangeInt(min = 0, max = 100000)
            public int energyCostCopyPaste = 50;
            @Comment("The max durability of the Copy & Paste Gadget (Ignored if powered by FE)")
            @RangeInt(min = 0, max = 100000)
            public int durabilityCopyPaste = 500;
        }
    }


}

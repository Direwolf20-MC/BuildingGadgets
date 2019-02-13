package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableList;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

import static net.minecraftforge.common.ForgeConfigSpec.*;
import static net.minecraftforge.fml.Logging.CORE;

public class Config {

    private static final String CATEGORY_GENERAL = "general";

    private static final String LANG_KEY_ROOT = "config." + BuildingGadgets.MODID;

    private static final String LANG_KEY_GENERAL = LANG_KEY_ROOT + "." + CATEGORY_GENERAL;

    private static final String LANG_KEY_BLACKLIST = LANG_KEY_ROOT + ".blacklist";

    private static final String LANG_KEY_GADGETS = LANG_KEY_ROOT + ".gadgets";

    private static final String LANG_KEY_PASTE_CONTAINERS = LANG_KEY_ROOT + ".subCategoryPasteContainers";

    private static final String LANG_KEY_GADGET_BUILDING = LANG_KEY_GADGETS + ".gadgetBuilding";

    private static final String LANG_KEY_GADGET_EXCHANGER = LANG_KEY_GADGETS + ".gadgetExchanger";

    private static final String LANG_KEY_GADGET_DESTRUCTION = LANG_KEY_GADGETS + ".gadgetDestruction";

<<<<<<< HEAD
    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final CategoryGadgets GADGETS = new CategoryGadgets();

    public static final CategoryBlacklist BLACKLIST = new CategoryBlacklist();

    public static final class CategoryGeneral {

        public final DoubleValue rayTraceRange;

        public final BooleanValue enablePaste;
=======
    private static final String LANG_KEY_GADGET_COPY_PASTE = LANG_KEY_GADGETS + ".gadgetCopyPaste";

    private static final String LANG_KEY_PASTE_CONTAINERS_CAPACITY = LANG_KEY_PASTE_CONTAINERS + ".capacity";

    private static final String LANG_KEY_GADGETS_ENERGY = LANG_KEY_GADGETS + ".energyCost";

    private static final String LANG_KEY_GADGETS_DAMAGE = LANG_KEY_GADGETS + ".damageCost";

    private static final String LANG_KEY_GADGETS_DURABILITY = LANG_KEY_GADGETS + ".durability";

    private static final String LANG_KEY_GADGETS_ENERGY_COMMENT = "The Gadget's Energy cost per Operation";

    private static final String LANG_KEY_GADGETS_DAMAGE_COMMENT = "The Gadget's Damage cost per Operation";

    private static final String LANG_KEY_GADGETS_DURABILITY_COMMENT = "The Gadget's Durability (0 means no durability is used) (Ignored if powered by FE)";

    @RangeDouble(min = 1 , max = 48)
    @Name("Max Build Distance")
    @Comment("Defines how far away you can build")
    @LangKey(LANG_KEY_ROOT + ".rayTraceRange")
    public static double rayTraceRange = 32;

    @RequiresWorldRestart
    @Name("Powered by Forge Energy")
    @Comment("Set to true for Forge Energy Support, set to False for vanilla Item Damage")
    @LangKey(LANG_KEY_ROOT + ".poweredByFE")
    public static boolean poweredByFE = true;

    @RequiresMcRestart
    @RequiresWorldRestart
    @Name("Enable Construction Paste")
    @Comment("Set to false to disable the recipe for construction paste.")
    @LangKey(LANG_KEY_ROOT + ".enablePaste")
    public static boolean enablePaste = true;

    @RequiresMcRestart
    @RequiresWorldRestart
    @Name("Enable Destruction Gadget")
    @Comment("Set to false to disable the Destruction Gadget.")
    @LangKey(LANG_KEY_ROOT + ".enableDestructionGadget")
    public static boolean enableDestructionGadget = true;

    @Name("Default to absolute Coord-Mode")
    @Comment({"Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.",
              "Set to true for Absolute, set to False for Relative."})
    @LangKey(LANG_KEY_ROOT + ".absoluteCoordDefault")
    public static boolean absoluteCoordDefault = false;

    @Name("Allow non-Air-Block-Overwrite")
    @Comment({"Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).",
              "False will only allow it to overwrite air blocks."})
    @LangKey(LANG_KEY_ROOT + ".canOverwriteBlocks")
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
        @LangKey(LANG_KEY_BLACKLIST + " + blockBlacklist")
        public String[] blockBlacklist = {"minecraft:.*_door.*", PatternList.getName(Blocks.PISTON_HEAD)};
    }
>>>>>>> 7e6c7a3a9739c1d5729472f553b8eba2c2aca491

        public final BooleanValue enableDestructionGadget;

        /* Client Only!*/
        public final BooleanValue absoluteCoordDefault;

        public final BooleanValue allowOverwriteBlocks;

        private CategoryGeneral() {
            SERVER_BUILDER.comment("General mod settings")/*.translation(LANG_KEY_GENERAL)*/.push("general");
            CLIENT_BUILDER.comment("General mod settings")/*.translation(LANG_KEY_GENERAL)*/.push("general");
            rayTraceRange = SERVER_BUILDER
                    .comment("Defines how far away you can build")
                    .translation(LANG_KEY_GENERAL + ".rayTraceRange")
                    .defineInRange("MaxBuildDistance", 32D, 1, 48);

            enablePaste = SERVER_BUILDER
                    .comment("Set to false to disable the recipe for construction paste.")
                    .translation(LANG_KEY_GENERAL + ".enablePaste")
                    .define("Enable Construction Paste", true);

            enableDestructionGadget = SERVER_BUILDER
                    .comment("Set to false to disable the Destruction Gadget.")
                    .translation(LANG_KEY_GENERAL + ".enableDestructionGadget")
                    .define("Enable Destruction Gadget", true);

            absoluteCoordDefault = CLIENT_BUILDER
                    .comment("Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.", "Set to true for Absolute, set to False for Relative.")
                    .translation(LANG_KEY_GENERAL + ".absoluteCoordDefault")
                    .define("Default to absolute Coord-Mode", false);

            allowOverwriteBlocks = SERVER_BUILDER
                    .comment("Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).",
                            "False will only allow it to overwrite air blocks.")
                    .translation(LANG_KEY_GENERAL + ".allowOverwriteBlocks")
                    .define("Allow non-Air-Block-Overwrite", true);
            CLIENT_BUILDER.pop();
            SERVER_BUILDER.pop();
        }
    }

    @Name("Paste Containers")
    @Comment("Configure the Paste Containers here")
    @LangKey(LANG_KEY_PASTE_CONTAINERS)
    public static CategoryPasteContainers subCategoryPasteContainers = new CategoryPasteContainers();

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
<<<<<<< HEAD
        public final IntValue maxRange;

        public final BooleanValue poweredByFE;

        public final CategoryGadgetBuilding GADGET_BUILDING;

        public final CategoryGadgetExchanger GADGET_EXCHANGER;

        public final CategoryGadgetDestruction GADGET_DESTRUCTION;

        public final CategoryGadgetCopyPaste GADGET_COPY_PASTE;

        private CategoryGadgets() {
            SERVER_BUILDER.comment("Configure the Gadgets here")/*.translation(LANG_KEY_GADGETS)*/.push("Gadgets");

            maxRange = SERVER_BUILDER
                    .comment("The max range of the Gadgets")
                    .translation(LANG_KEY_GADGETS + ".maxRange")
                    .defineInRange("Maximum allowed Range", 16, 1, 32);

            poweredByFE = SERVER_BUILDER
                    .comment("Set to true for Forge Energy Support, set to False for vanilla Item Damage")
                    .translation(LANG_KEY_GENERAL + ".poweredByFE")
                    .define("Powered by Forge Energy", true);

            GADGET_BUILDING = new CategoryGadgetBuilding();
            GADGET_EXCHANGER = new CategoryGadgetExchanger();
            GADGET_DESTRUCTION = new CategoryGadgetDestruction();
            GADGET_COPY_PASTE = new CategoryGadgetCopyPaste();
            SERVER_BUILDER.pop();
        }

        public static final class CategoryGadgetBuilding {
            //TODO @since 1.13.x add durabilityCost Config option
            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;

            private CategoryGadgetBuilding() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Building Gadget")/*.translation(LANG_KEY_GADGET_BUILDING)*/.push("Building Gadget");

                energyCost = SERVER_BUILDER
                        .comment("The energy cost of the Builder per block")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 50, 0, Integer.MAX_VALUE);

                maxEnergy = SERVER_BUILDER
                        .comment("The max energy of the Gadget")
                        .translation(LANG_KEY_GADGETS + ".maxEnergy")
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment("The max durability of the Builder (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetExchanger {

            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;

            private CategoryGadgetExchanger() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Exchanging Gadget")/*.translation(LANG_KEY_GADGET_EXCHANGER)*/.push("Exchanging Gadget");

                energyCost = SERVER_BUILDER
                        .comment("The energy cost of the Exchanger per block")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 100, 0, Integer.MAX_VALUE);

                maxEnergy = SERVER_BUILDER
                        .comment("The max energy of the Gadget")
                        .translation(LANG_KEY_GADGETS + ".maxEnergy")
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment("The max durability of the Exchanger (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetDestruction {

            public final IntValue maxEnergy;

            public final IntValue energyCost;

            public final IntValue durability;

            private CategoryGadgetDestruction() {
                SERVER_BUILDER.comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")/*.translation(LANG_KEY_GADGET_DESTRUCTION)*/.push("Destruction Gadget");

                energyCost = SERVER_BUILDER
                        .comment("The energy cost of the Destruction Gadget per block")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 200, 0, Integer.MAX_VALUE);

                maxEnergy = SERVER_BUILDER
                        .comment("The max energy of the Gadget")
                        .translation(LANG_KEY_GADGETS + ".maxEnergy")
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment("The max durability of the Destruction Gadget (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();

            }
        }

        public static final class CategoryGadgetCopyPaste {

            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;


            private CategoryGadgetCopyPaste() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Copy-Paste Gadget")/*.translation(LANG_KEY_GADGET_COPY_PASTE)*/.push("Copy-Paste Gadget");

                energyCost = SERVER_BUILDER
                        .comment("The Energy Use of the Copy Paste Gadget")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 50, 0, Integer.MAX_VALUE);

                maxEnergy = SERVER_BUILDER
                        .comment("The max energy of the Gadget")
                        .translation(LANG_KEY_GADGETS + ".maxEnergy")
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment("The max durability of the Copy & Paste Gadget (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }
    }

    public static final class CategoryBlacklist {
        public final ConfigValue<List<? extends String>> blockBlacklist; //TODO convert to a tag (or at least make compatible with) - I don't know whether this might or might not work

        private CategoryBlacklist() {
            SERVER_BUILDER.comment("Configure your Blacklist-Settings here")/*.translation(LANG_KEY_BLACKLIST)*/.push("Blacklist Settings");

            blockBlacklist = SERVER_BUILDER
                    .comment("All Blocks added to this will be treated similar to TileEntities. Not at all.",
                            "Notice that you can use Regular Expressions as defined by Java Patterns to express more complex name combinations.",
                            "Use for example \"awfulmod:.*\" to blacklist all awfulmod Blocks.")
                    .translation(LANG_KEY_BLACKLIST + ".blockBlacklist")
                    .defineList("Blacklisted Blocks", ImmutableList.of("minecraft:.*_door.*", PatternList.getName(Blocks.PISTON_HEAD)), obj -> obj instanceof String);

            SERVER_BUILDER.pop();

        }


    }

    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        BuildingGadgets.LOG.debug("Loaded {} config file {}", BuildingGadgets.MODID, configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        BuildingGadgets.LOG.fatal(CORE, "{} config just got changed on the file system!", BuildingGadgets.MODID);
=======
        private CategoryGadgets() {
        }

        @RangeInt(min = 1, max = 25)
        @Name("Maximum allowed Range")
        @Comment("The max range of the Gadgets")
        @LangKey(LANG_KEY_GADGETS + ".maxRange")
        public int maxRange = 15;

        @RangeInt(min = 0)
        @Name("Maximum Energy")
        @Comment("The max energy of Building, Exchanging & Copy-Paste Gadget")
        @LangKey(LANG_KEY_GADGETS + ".maxEnergy")
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
            @Comment(LANG_KEY_GADGETS_ENERGY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_ENERGY)
            public int energyCostBuilder = 50;
 
            @RangeInt(min = 0, max = 2000)
            @Name("Damage Cost")
            @Comment(LANG_KEY_GADGETS_DAMAGE_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DAMAGE)
            public int damageCostBuilder = 1;

            @RequiresWorldRestart
            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment(LANG_KEY_GADGETS_DURABILITY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DURABILITY)
            public int durabilityBuilder = 500;
        }

        public static final class CategoryGadgetExchanger {
            private CategoryGadgetExchanger(){ }

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment(LANG_KEY_GADGETS_ENERGY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_ENERGY)
            public int energyCostExchanger = 100;

            @RangeInt(min = 0, max = 2000)
            @Name("Damage Cost")
            @Comment(LANG_KEY_GADGETS_DAMAGE_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DAMAGE)
            public int damageCostExchanger = 2;

            @RequiresWorldRestart
            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment(LANG_KEY_GADGETS_DURABILITY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DURABILITY)
            public int durabilityExchanger = 500;
        }

        public static final class CategoryGadgetDestruction {
            private CategoryGadgetDestruction () {}
            @RangeInt(min = 0)
            @Name("Maximum Energy")
            @Comment("The max energy of the Destruction Gadget")
            @LangKey(LANG_KEY_GADGET_DESTRUCTION + ".maxEnergy")
            public int energyMaxDestruction = 1000000;

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment(LANG_KEY_GADGETS_ENERGY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_ENERGY)
            public int energyCostDestruction = 200;

            @RangeInt(min = 0, max = 2000)
            @Name("Damage Cost")
            @Comment(LANG_KEY_GADGETS_DAMAGE_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DAMAGE)
            public int damageCostDestruction = 4;

            @RequiresWorldRestart
            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment(LANG_KEY_GADGETS_DURABILITY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DURABILITY)
            public int durabilityDestruction = 500;

            @RangeDouble(min = 0)
            @Name("Non-Fuzzy Mode Multiplier")
            @Comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
            @LangKey(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.multiplier")
            public double nonFuzzyMultiplier = 2;

            @Name("Non-Fuzzy Mode Enabled")
            @Comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block "
                    + "clicked to be removed (at a higher cost)")
            @LangKey(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.enabled")
            public boolean nonFuzzyEnabled = false;
        }

        public static final class CategoryGadgetCopyPaste {
            private CategoryGadgetCopyPaste() { }

            @RangeInt(min = 0, max = 100000)
            @Name("Energy Cost")
            @Comment(LANG_KEY_GADGETS_ENERGY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_ENERGY)
            public int energyCostCopyPaste = 50;

            @RangeInt(min = 0, max = 2000)
            @Name("Damage Cost")
            @Comment(LANG_KEY_GADGETS_DAMAGE_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DAMAGE)
            public int damageCostCopyPaste = 1;

            @RequiresWorldRestart
            @RangeInt(min = 0, max = 100000)
            @Name("Durability")
            @Comment(LANG_KEY_GADGETS_DURABILITY_COMMENT)
            @LangKey(LANG_KEY_GADGETS_DURABILITY)
            public int durabilityCopyPaste = 500;
        }
    }

    public static final class CategoryPasteContainers {
        private CategoryPasteContainers() {
        }

        @RangeInt(min = 1)
        @Comment("The maximum capacity of a tier 1 (iron) Construction Paste Container")
        @Name("T1 Container Capacity")
        @LangKey(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t1")
        public int t1Capacity = 512;

        @RangeInt(min = 1)
        @Comment("The maximum capacity of a tier 2 (gold) Construction Paste Container")
        @Name("T2 Container Capacity")
        @LangKey(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t2")
        public int t2Capacity = 2048;

        @RangeInt(min = 1)
        @Comment("The maximum capacity of a tier 3 (diamond) Construction Paste Container")
        @Name("T3 Container Capacity")
        @LangKey(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t3")
        public int t3Capacity = 8192;
>>>>>>> 7e6c7a3a9739c1d5729472f553b8eba2c2aca491
    }

}

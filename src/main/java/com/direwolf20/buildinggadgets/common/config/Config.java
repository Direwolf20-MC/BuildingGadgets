package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.Objects;

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

    private static final String LANG_KEY_GADGETS_ENERGY = LANG_KEY_GADGETS + ".maxEnergy";

    private static final String LANG_KEY_GADGETS_ENERGY_COST = LANG_KEY_GADGETS + ".energyCost";

    private static final String LANG_KEY_GADGETS_DURABILITY = LANG_KEY_GADGETS + ".durability";

    private static final String LANG_KEY_GADGETS_DURABILITY_COST = LANG_KEY_GADGETS + ".durabilityCost";

    private static final String LANG_KEY_PASTE_CONTAINERS_CAPACITY = LANG_KEY_PASTE_CONTAINERS + ".capacity";

    private static final String COMMENT_GADGETS_ENERGY = "The max energy of the Gadget";

    private static final String COMMENT_GADGETS_ENERGY_COST = "The Gadget's Energy cost per Operation";

    private static final String COMMENT_GADGETS_DURABILITY = "The Gadget's Durability (0 means no durability is used) (Ignored if powered by FE)";

    private static final String COMMENT_GADGETS_DURABILITY_COST = "The Gadget's Damage cost per Operation";


    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final CategoryGadgets GADGETS = new CategoryGadgets();

    public static final CategoryBlacklist BLACKLIST = new CategoryBlacklist();

    public static final class CategoryGeneral {

        public final DoubleValue rayTraceRange;

        public final BooleanValue enablePaste;

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

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
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

            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;

            public final IntValue durabilityCost;

            private CategoryGadgetBuilding() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Building Gadget")/*.translation(LANG_KEY_GADGET_BUILDING)*/.push("Building Gadget");

                maxEnergy = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY)
                        .translation(LANG_KEY_GADGETS_ENERGY)
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                energyCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY_COST)
                        .translation(LANG_KEY_GADGETS_ENERGY_COST)
                        .defineInRange("Energy Cost", 50, 0, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY)
                        .translation(LANG_KEY_GADGETS_DURABILITY)
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                durabilityCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY_COST)
                        .translation(LANG_KEY_GADGETS_DURABILITY_COST)
                        .defineInRange("Damage Cost",1,0,Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetExchanger {

            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;

            public final IntValue durabilityCost;

            private CategoryGadgetExchanger() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Exchanging Gadget")/*.translation(LANG_KEY_GADGET_EXCHANGER)*/.push("Exchanging Gadget");

                maxEnergy = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY)
                        .translation(LANG_KEY_GADGETS_ENERGY)
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                energyCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY_COST)
                        .translation(LANG_KEY_GADGETS_ENERGY_COST)
                        .defineInRange("Energy Cost", 100, 0, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY)
                        .translation(LANG_KEY_GADGETS_DURABILITY)
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                durabilityCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY_COST)
                        .translation(LANG_KEY_GADGETS_DURABILITY_COST)
                        .defineInRange("Damage Cost",2,0,Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetDestruction {

            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;

            public final IntValue durabilityCost;

            public final DoubleValue nonFuzzyMultiplier;

            public final BooleanValue nonFuzzyEnabled;

            private CategoryGadgetDestruction() {
                SERVER_BUILDER.comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")/*.translation(LANG_KEY_GADGET_DESTRUCTION)*/.push("Destruction Gadget");

                maxEnergy = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY)
                        .translation(LANG_KEY_GADGETS_ENERGY)
                        .defineInRange("Maximum Energy", 1000000, 1, Integer.MAX_VALUE);

                energyCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY_COST)
                        .translation(LANG_KEY_GADGETS_ENERGY_COST)
                        .defineInRange("Energy Cost", 200, 0, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY)
                        .translation(LANG_KEY_GADGETS_DURABILITY)
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                durabilityCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY_COST)
                        .translation(LANG_KEY_GADGETS_DURABILITY_COST)
                        .defineInRange("Damage Cost",4,0,Integer.MAX_VALUE);

                nonFuzzyMultiplier = SERVER_BUILDER
                        .comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
                        .translation(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.multiplier")
                        .defineInRange("Non-Fuzzy Mode Multiplier",2,0,Double.MAX_VALUE);

                nonFuzzyEnabled = SERVER_BUILDER
                        .comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block clicked to be removed (at a higher cost)")
                        .translation(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.enabled")
                        .define("Non-Fuzzy Mode Enabled",false);

                SERVER_BUILDER.pop();

            }
        }

        public static final class CategoryGadgetCopyPaste {

            public final IntValue energyCost;

            public final IntValue maxEnergy;

            public final IntValue durability;

            public final IntValue durabilityCost;


            private CategoryGadgetCopyPaste() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Copy-Paste Gadget")/*.translation(LANG_KEY_GADGET_COPY_PASTE)*/.push("Copy-Paste Gadget");

                maxEnergy = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY)
                        .translation(LANG_KEY_GADGETS_ENERGY)
                        .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

                energyCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_ENERGY_COST)
                        .translation(LANG_KEY_GADGETS_ENERGY_COST)
                        .defineInRange("Energy Cost", 50, 0, Integer.MAX_VALUE);

                durability = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY)
                        .translation(LANG_KEY_GADGETS_DURABILITY)
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                durabilityCost = SERVER_BUILDER
                        .comment(COMMENT_GADGETS_DURABILITY_COST)
                        .translation(LANG_KEY_GADGETS_DURABILITY_COST)
                        .defineInRange("Damage Cost",1,0,Integer.MAX_VALUE);

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

        public boolean isAllowedBlock(Block block) {
            //TODO replace with Patternlist
            return blockBlacklist.get().contains(Objects.requireNonNull(block.getRegistryName()).toString());
        }

    }

    public static final class CategoryPasteContainers {

        public final IntValue t1Capacity;

        public final IntValue t2Capacity;

        public final IntValue t3Capacity;

        private CategoryPasteContainers() {
            SERVER_BUILDER.comment("Configure the Paste Containers here")/*.translation(LANG_KEY_PASTE_CONTAINERS)*/.push("Paste Containers");

            t1Capacity = SERVER_BUILDER
                    .comment("The maximum capacity of a tier 1 (iron) Construction Paste Container")
                    .translation(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t1")
                    .defineInRange("T1 Container Capacity",512,0,Integer.MAX_VALUE);

            t2Capacity = SERVER_BUILDER
                    .comment("The maximum capacity of a tier 2 (gold) Construction Paste Container")
                    .translation(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t2")
                    .defineInRange("T2 Container Capacity",2048,0,Integer.MAX_VALUE);

            t3Capacity = SERVER_BUILDER
                    .comment("The maximum capacity of a tier 3 (diamond) Construction Paste Container")
                    .translation(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t3")
                    .defineInRange("T3 Container Capacity",8192,0,Integer.MAX_VALUE);

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
    }

}

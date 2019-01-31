package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableList;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.util.List;

import static net.minecraftforge.common.ForgeConfigSpec.*;
import static net.minecraftforge.fml.Logging.CORE;
public class Config {

    private static final String CATEGORY_GENERAL = "general";

    private static final String LANG_KEY_ROOT = "config." + BuildingGadgets.MODID;

    private static final String LANG_KEY_GENERAL = LANG_KEY_ROOT + "." + CATEGORY_GENERAL;

    private static final String LANG_KEY_BLACKLIST = LANG_KEY_ROOT + ".blacklist";

    private static final String LANG_KEY_GADGETS = LANG_KEY_ROOT + ".gadgets";

    private static final String LANG_KEY_GADGET_BUILDING = LANG_KEY_GADGETS+".gadgetBuilding";

    private static final String LANG_KEY_GADGET_EXCHANGER = LANG_KEY_GADGETS+".gadgetExchanger";

    private static final String LANG_KEY_GADGET_DESTRUCTION = LANG_KEY_GADGETS+".gadgetDestruction";

    private static final String LANG_KEY_GADGET_COPY_PASTE = LANG_KEY_GADGETS+".gadgetCopyPaste";

    private static final Builder BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final CategoryGadgets GADGETS = new CategoryGadgets();

    public static final CategoryBlacklist BLACKLIST = new CategoryBlacklist();

    public static final class CategoryGeneral {

        public final DoubleValue rayTraceRange;

        public final BooleanValue poweredByFE;

        public final BooleanValue enablePaste;

        public final BooleanValue enableDestructionGadget;

        public final BooleanValue absoluteCoordDefault;

        public final BooleanValue canOverwriteBlocks;

        private CategoryGeneral() {
            BUILDER.comment("General mod settings").translation(LANG_KEY_GENERAL).push("general");

            rayTraceRange = BUILDER
                    .comment("Defines how far away you can build")
                    .translation(LANG_KEY_GENERAL + ".rayTraceRange")
                    .defineInRange("MaxBuildDistance", 32D, 1, 48);

            poweredByFE = BUILDER
                    .comment("Set to true for Forge Energy Support, set to False for vanilla Item Damage")
                    .translation(LANG_KEY_GENERAL + ".poweredByFE")
                    .define("Powered by Forge Energy", true);

            enablePaste = BUILDER
                    .comment("Set to false to disable the recipe for construction paste.")
                    .translation(LANG_KEY_GENERAL + ".enablePaste")
                    .define("Enable Construction Paste", true);

            enableDestructionGadget = BUILDER
                    .comment("Set to false to disable the Destruction Gadget.")
                    .translation(LANG_KEY_GENERAL + ".enableDestructionGadget")
                    .define("Enable Destruction Gadget", true);

            absoluteCoordDefault = BUILDER
                    .comment("Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.", "Set to true for Absolute, set to False for Relative.")
                    .translation(LANG_KEY_GENERAL + ".absoluteCoordDefault")
                    .define("Default to absolute Coord-Mode", false);

            canOverwriteBlocks = BUILDER
                    .comment("Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).",
                            "False will only allow it to overwrite air blocks.")
                    .translation(LANG_KEY_GENERAL + ".canOverwriteBlocks")
                    .define("Allow non-Air-Block-Overwrite", true);
            BUILDER.pop();
        }
    }

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {

        public final IntValue maxRange;

        public final IntValue maxEnergy;

        public final CategoryGadgetBuilding subCategoryGadgetBuilding;

        public final CategoryGadgetExchanger subCategoryGadgetExchanger;

        public final CategoryGadgetDestruction subCategoryGadgetDestruction;

        public final CategoryGadgetCopyPaste subCategoryGadgetCopyPaste;

        private CategoryGadgets() {
            BUILDER.comment("Configure the Gadgets here").translation(LANG_KEY_GADGETS).push("Gadgets");

            maxRange = BUILDER
                    .comment("The max range of the Gadgets")
                    .translation(LANG_KEY_GADGETS + ".maxRange")
                    .defineInRange("Maximum allowed Range", 16, 1, 32);

            maxEnergy = BUILDER
                    .comment("The max energy of Building, Exchanging & Copy-Paste Gadget")
                    .translation(LANG_KEY_GADGETS + ".maxEnergy")
                    .defineInRange("Maximum Energy", 500000, 1, Integer.MAX_VALUE);

            subCategoryGadgetBuilding = new CategoryGadgetBuilding();
            subCategoryGadgetExchanger = new CategoryGadgetExchanger();
            subCategoryGadgetDestruction = new CategoryGadgetDestruction();
            subCategoryGadgetCopyPaste = new CategoryGadgetCopyPaste();
            BUILDER.pop();
        }

        public static final class CategoryGadgetBuilding {

            public final IntValue energyCost;

            public final IntValue durability;

            private CategoryGadgetBuilding() {
                BUILDER.comment("Energy Cost & Durability of the Building Gadget").translation(LANG_KEY_GADGET_BUILDING).push("Building Gadget");

                energyCost = BUILDER
                        .comment("The energy cost of the Builder per block")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 50, 0, Integer.MAX_VALUE);

                durability = BUILDER
                        .comment("The max durability of the Builder (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }
        }

        public static final class CategoryGadgetExchanger {

            public final IntValue energyCost;

            public final IntValue durability;

            private CategoryGadgetExchanger() {
                BUILDER.comment("Energy Cost & Durability of the Exchanging Gadget").translation(LANG_KEY_GADGET_EXCHANGER).push("Exchanging Gadget");

                energyCost = BUILDER
                        .comment("The energy cost of the Exchanger per block")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 100, 0, Integer.MAX_VALUE);

                durability = BUILDER
                        .comment("The max durability of the Exchanger (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }
        }

        public static final class CategoryGadgetDestruction {

            public final IntValue energyMax;

            public final IntValue energyCost;

            public final IntValue durability;

            private CategoryGadgetDestruction() {
                BUILDER.comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget").translation(LANG_KEY_GADGET_DESTRUCTION).push("Destruction Gadget");

                energyMax = BUILDER
                        .comment("The max energy of the Destruction Gadget")
                        .translation(LANG_KEY_GADGET_DESTRUCTION + ".maxEnergy")
                        .defineInRange("Maximum Energy", 1000000, 1, Integer.MAX_VALUE);

                energyCost = BUILDER
                        .comment("The energy cost of the Destruction Gadget per block")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 200, 0, Integer.MAX_VALUE);

                durability = BUILDER
                        .comment("The max durability of the Destruction Gadget (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                BUILDER.pop();

            }
        }

        public static final class CategoryGadgetCopyPaste {

            public final IntValue energyCost;

            public final IntValue durability;


            private CategoryGadgetCopyPaste() {
                BUILDER.comment("Energy Cost & Durability of the Copy-Paste Gadget").translation(LANG_KEY_GADGET_COPY_PASTE).push("Copy-Paste Gadget");

                energyCost = BUILDER
                        .comment("The Energy Use of the Copy Paste Gadget")
                        .translation(LANG_KEY_GADGETS + ".energyCost")
                        .defineInRange("Energy Cost", 50, 0, Integer.MAX_VALUE);

                durability = BUILDER
                        .comment("The max durability of the Copy & Paste Gadget (Ignored if powered by FE)")
                        .translation(LANG_KEY_GADGETS + ".durability")
                        .defineInRange("Durability", 500, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }
        }
    }

    public static final class CategoryBlacklist {
        public final ConfigValue<List<? extends String>> blockBlacklist; //TODO convert to a tag (or at least make compatible with) - I don't know whether this might or might not work

        private CategoryBlacklist() {
            BUILDER.comment("Configure your Blacklist-Settings here").translation(LANG_KEY_BLACKLIST).push("Blacklist Settings");

            blockBlacklist = BUILDER
                    .comment("All Blocks added to this will be treated similar to TileEntities. Not at all.",
                            "Notice that you can use Regular Expressions as defined by Java Patterns to express more complex name combinations.",
                            "Use for example \"awfulmod:.*\" to blacklist all awfulmod Blocks.")
                    .translation(LANG_KEY_BLACKLIST + ".blockBlacklist")
                    .defineList("Blacklisted Blocks", ImmutableList.of("minecraft:.*_door.*", PatternList.getName(Blocks.PISTON_HEAD)), obj -> obj instanceof String);

            BUILDER.pop();

        }


    }

    private static final ForgeConfigSpec CONFIG = BUILDER.build();
    private static final String CONFIG_FILE = BuildingGadgets.MODNAME + ".toml";

    private static void loadFrom(final Path configRoot) {
        Path configFile = configRoot.resolve(CONFIG_FILE);
//        CONFIG.setConfigFile(configFile);
        LogManager.getLogger().debug(CORE, "Loaded {} config from {}", BuildingGadgets.MODID, configFile);
    }

    public static void load() {
        loadFrom(FMLPaths.CONFIGDIR.get());
    }

}

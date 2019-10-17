package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;

import static net.minecraftforge.common.ForgeConfigSpec.*;
import static net.minecraftforge.fml.Logging.CORE;

@EventBusSubscriber
public class Config {

    private static final String CATEGORY_GENERAL = "general";

    private static final String LANG_KEY_ROOT = "config." + Reference.MODID;
    private static final String LANG_KEY_GENERAL = LANG_KEY_ROOT + "." + CATEGORY_GENERAL;
    private static final String LANG_KEY_CHARGING_STATION = LANG_KEY_ROOT + ".charging";
    private static final String LANG_KEY_BLACKLIST = LANG_KEY_ROOT + ".blacklist";
    private static final String LANG_KEY_GADGETS = LANG_KEY_ROOT + ".gadgets";
    private static final String LANG_KEY_PASTE_CONTAINERS = LANG_KEY_ROOT + ".pasteContainers";
    private static final String LANG_KEY_PASTE_CONTAINERS_CAPACITY = LANG_KEY_PASTE_CONTAINERS + ".capacity";
    private static final String LANG_KEY_GADGET_BUILDING = LANG_KEY_GADGETS + ".gadgetBuilding";
    private static final String LANG_KEY_GADGET_EXCHANGER = LANG_KEY_GADGETS + ".gadgetExchanger";
    private static final String LANG_KEY_GADGET_DESTRUCTION = LANG_KEY_GADGETS + ".gadgetDestruction";
    private static final String LANG_KEY_GADGET_COPY_PASTE = LANG_KEY_GADGETS + ".gadgetCopyPaste";

    private static final Builder COMMON_BUILDER = new Builder();
    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();
    public static final CategoryChargingStation CHARGING_STATION = new CategoryChargingStation();
    public static final CategoryGadgets GADGETS = new CategoryGadgets();
    public static final CategoryPasteContainers PASTE_CONTAINERS = new CategoryPasteContainers();
    public static final CategoryBlacklist BLACKLIST = new CategoryBlacklist();

    public static final class CategoryGeneral {

        public final DoubleValue rayTraceRange;

        /* Client Only!*/
        public final BooleanValue absoluteCoordDefault;
        public final BooleanValue allowOverwriteBlocks;

        private CategoryGeneral() {
            SERVER_BUILDER.comment("General mod settings")/*.translation(LANG_KEY_GENERAL)*/.push("general");
            COMMON_BUILDER.comment("General mod settings")/*.translation(LANG_KEY_GENERAL)*/.push("general");
            CLIENT_BUILDER.comment("General mod settings")/*.translation(LANG_KEY_GENERAL)*/.push("general");
            rayTraceRange = SERVER_BUILDER
                    .comment("Defines how far away you can build")
                    .translation(LANG_KEY_GENERAL + ".rayTraceRange")
                    .defineInRange("MaxBuildDistance", 32D, 1, 48);

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
            COMMON_BUILDER.pop();
            SERVER_BUILDER.pop();
        }
    }

    public static final class CategoryChargingStation {
        public final IntValue capacity;
        public final DoubleValue fuelUsage;
        public final IntValue energyPerTick;
        public final IntValue chargePerTick;
        public final IntValue maxRecieve;
        public final IntValue maxExtract;
        public final BooleanValue renderSphere;

        private CategoryChargingStation() {
            SERVER_BUILDER.comment("Charging station settings")/*.translation(LANG_KEY_CHARGING_STATION)*/.push("charging_station");
            COMMON_BUILDER.comment("Charging station settings")/*.translation(LANG_KEY_CHARGING_STATION)*/.push("charging_station");
            CLIENT_BUILDER.comment("Charging station settings")/*.translation(LANG_KEY_CHARGING_STATION)*/.push("charging_station");
            capacity = SERVER_BUILDER.comment("Define the maximum energy stored in the Charging Station.")
                    .translation(LANG_KEY_CHARGING_STATION + ".capacity")
                    .defineInRange("Energy capacity", 1000000, 1, Integer.MAX_VALUE);
            fuelUsage = SERVER_BUILDER.comment("Define the speed factor, by which the fuel is going to be consumed.")
                    .translation(LANG_KEY_CHARGING_STATION + ".consume")
                    .defineInRange("Fuel Usage", 50, 0.00000001, Double.MAX_VALUE);
            energyPerTick = SERVER_BUILDER.comment("Define the amount of energy produced per fuel tick.")
                    .translation(LANG_KEY_CHARGING_STATION + ".fePerTick")
                    .defineInRange("Energy Production", 750, 0, Integer.MAX_VALUE);
            chargePerTick = SERVER_BUILDER.comment("Define how much the charging station attempts to charge per tick")
                    .translation(LANG_KEY_CHARGING_STATION + ".chargePerTick")
                    .defineInRange("Charge Energy", 2500, 0, Integer.MAX_VALUE);
            maxRecieve = SERVER_BUILDER.comment("Define how much Energy can be accepted by the Charging Station per tick. Note that burning fuel counts towards this cap.")
                    .translation(LANG_KEY_CHARGING_STATION + ".maxRecieve")
                    .defineInRange("Max Energy Recieved", 750, 0, Integer.MAX_VALUE);
            maxExtract = SERVER_BUILDER.comment("Define how much Energy can at most be extracted from the Charging Station per tick. Charging Items does not" +
                    "count towards this cap!")
                    .translation(LANG_KEY_CHARGING_STATION + ".maxExtract")
                    .defineInRange("Max Energy Extracted", 0, 0, Integer.MAX_VALUE);
            renderSphere = CLIENT_BUILDER.comment("Whether or not to render the Charging Sphere. Disabling rendering of the Charging Sphere will increase rendering" +
                    "Performance, as it is the main throttle on the Charging Station render. Note that we are already doing what we can to make it as fast as possible " +
                    "but a colour changing Sphere just needs to update relativingly often...")
                    .translation(LANG_KEY_CHARGING_STATION + ".renderSphere")
                    .define("Render Sphere", true);
            CLIENT_BUILDER.pop();
            COMMON_BUILDER.pop();
            SERVER_BUILDER.pop();
        }
    }

    //using unistantiable final class instead of enum, so that it doesn't cause issues with the ConfigManger trying to access the Instance field
    //No defense against reflection needed here (I think)
    public static final class CategoryGadgets {
        public final IntValue maxRange;
        public final CategoryGadgetBuilding GADGET_BUILDING;
        public final CategoryGadgetExchanger GADGET_EXCHANGER;
        public final CategoryGadgetDestruction GADGET_DESTRUCTION;
        public final CategoryGadgetCopyPaste GADGET_COPY_PASTE;

        private CategoryGadgets() {
            SERVER_BUILDER.comment("Configure the Gadgets")/*.translation(LANG_KEY_GADGETS)*/.push("Gadgets");
            COMMON_BUILDER.comment("Configure the Gadgets")/*.translation(LANG_KEY_GADGETS)*/.push("Gadgets");
            maxRange = SERVER_BUILDER
                    .comment("The max range of the Gadgets")
                    .translation(LANG_KEY_GADGETS + ".maxRange")
                    .defineInRange("Maximum allowed Range", 15, 1, 32);

            GADGET_BUILDING     = new CategoryGadgetBuilding();
            GADGET_EXCHANGER    = new CategoryGadgetExchanger();
            GADGET_DESTRUCTION  = new CategoryGadgetDestruction();
            GADGET_COPY_PASTE   = new CategoryGadgetCopyPaste();

            COMMON_BUILDER.pop();
            SERVER_BUILDER.pop();
        }

        private static IntValue getMaxEnergy(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The max energy of the Gadget")
                    .translation(LANG_KEY_GADGETS + ".maxEnergy")
                    .defineInRange("Maximum Energy", defaultValue, 1, Integer.MAX_VALUE);
        }

        private static IntValue getEnergyCost(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The Gadget's Energy cost per Operation")
                    .translation(LANG_KEY_GADGETS + ".energyCost")
                    .defineInRange("Energy Cost", defaultValue, 0, Integer.MAX_VALUE);
        }

        private static IntValue getMaxUndoSize(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The Gadget'S Max Undo size")
                    .translation(LANG_KEY_GADGETS + ".undo_size")
                    .defineInRange("Max Undo History Size", defaultValue, 0, 128);
        }

        public static final class CategoryGadgetBuilding {

            public final IntValue maxEnergy;
            public final IntValue energyCost;
            public final IntValue undoSize;

            private CategoryGadgetBuilding() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Building Gadget")/*.translation(LANG_KEY_GADGET_BUILDING)*/.push("Building Gadget");

                maxEnergy    = getMaxEnergy(500000);
                energyCost   = getEnergyCost(50);
                undoSize     = getMaxUndoSize(10);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetExchanger {

            public final IntValue maxEnergy;
            public final IntValue energyCost;
            public final IntValue undoSize;

            private CategoryGadgetExchanger() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Exchanging Gadget")/*.translation(LANG_KEY_GADGET_EXCHANGER)*/.push("Exchanging Gadget");

                maxEnergy   = getMaxEnergy(500000);
                energyCost  = getEnergyCost(100);
                undoSize    = getMaxUndoSize(10);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetDestruction {

            public final IntValue maxEnergy;
            public final IntValue energyCost;
            public final IntValue undoSize;
            public final DoubleValue nonFuzzyMultiplier;
            public final BooleanValue nonFuzzyEnabled;

            private CategoryGadgetDestruction() {
                SERVER_BUILDER.comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")/*.translation(LANG_KEY_GADGET_DESTRUCTION)*/.push("Destruction Gadget");

                maxEnergy   = getMaxEnergy(1000000);
                energyCost  = getEnergyCost(200);
                undoSize    = getMaxUndoSize(1);

                nonFuzzyMultiplier = SERVER_BUILDER
                        .comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
                        .translation(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.multiplier")
                        .defineInRange("Non-Fuzzy Mode Multiplier", 2, 0, Double.MAX_VALUE);

                nonFuzzyEnabled = SERVER_BUILDER
                        .comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block "
                                + "clicked to be removed (at a higher cost)")
                        .translation(LANG_KEY_GADGET_DESTRUCTION + ".nonfuzzy.enabled")
                        .define("Non-Fuzzy Mode Enabled", false);

                SERVER_BUILDER.pop();

            }
        }

        public static final class CategoryGadgetCopyPaste {

            public final IntValue maxEnergy;
            public final IntValue energyCost;
            public final IntValue undoSize;
            public final IntValue copySteps;
            public final IntValue placeSteps;
            public final IntValue maxCopySize;
            public final IntValue maxBuildSize;

            private CategoryGadgetCopyPaste() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Copy-Paste Gadget")/*.translation(LANG_KEY_GADGET_COPY_PASTE)*/.push("Copy-Paste Gadget");

                maxEnergy   = getMaxEnergy(500000);
                energyCost  = getEnergyCost(50);
                undoSize    = getMaxUndoSize(1);

                copySteps = SERVER_BUILDER
                        .comment("Maximum amount of Blocks to be copied in one Tick. ",
                                "Lower values may improve Server-Performance when copying large Templates")
                        .translation(LANG_KEY_GADGET_COPY_PASTE + ".copy_steps")
                        //use the old cap as the per tick border... This implies that 32*32*32 areas are the max size for a one tick copy by default
                        .defineInRange("Max Copy/Tick", 32768, 1, Integer.MAX_VALUE);

                placeSteps = SERVER_BUILDER
                        .comment("Maximum amount of Blocks to be placed in one Tick.",
                                "Notice that an EffectBlock takes 20 ticks to place, therefore a Server has to handle 20-times this value effect-block Tile's at once. " +
                                        "Reduce this if  you notice lag-spikes from Players placing Templates.",
                                "Of course decreasing this value will result in more time required to place large TemplateItem's.")
                        .translation(LANG_KEY_GADGET_COPY_PASTE + ".place_steps")
                        //use the old cap as the synchronous border... This implies that 32*32*32 areas are the max size for a synchronous copy by default
                        .defineInRange("Max Placement/Tick", 1024, 1, Integer.MAX_VALUE);

                maxCopySize = SERVER_BUILDER
                        .comment("Maximum dimensions (x, y and z) that can be copied by a Template without requiring special permission.",
                                "Permission can be granted using the '/buildinggadgets OverrideCopySize [<Player>]' command.")
                        .translation(LANG_KEY_GADGET_COPY_PASTE + ".max_copy")
                        .defineInRange("Max Copy Dimensions", 256, - 1, Integer.MAX_VALUE);

                maxBuildSize = SERVER_BUILDER
                        .comment("Maximum dimensions (x, y and z) that can be build by a Template without requiring special permission.",
                                "Permission can be granted using the '/buildinggadgets OverrideBuildSize [<Player>]' command.")
                        .translation(LANG_KEY_GADGET_COPY_PASTE + ".max_build")
                        .defineInRange("Max Build Dimensions", 256, - 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }
    }

    public static final class CategoryPasteContainers {

        public final IntValue capacityT1, capacityT2, capacityT3;

        private CategoryPasteContainers() {
            SERVER_BUILDER.comment("Configure the Paste Containers")/*.translation(LANG_KEY_PASTE_CONTAINERS)*/.push("Paste Containers");

            capacityT1 = getMaxCapacity(1);
            capacityT2 = getMaxCapacity(2);
            capacityT3 = getMaxCapacity(3);

            SERVER_BUILDER.pop();
        }

        private static IntValue getMaxCapacity(int tier) {
            return SERVER_BUILDER
                    .comment(String.format("The maximum capacity of a tier %s (iron) Construction Paste Container", tier))
                    .translation(LANG_KEY_PASTE_CONTAINERS_CAPACITY + ".t" + tier)
                    .defineInRange(String.format("T%s Container Capacity", tier), (int) (512 * Math.pow(4, tier - 1)), 1, Integer.MAX_VALUE);
        }
    }

    public static final class CategoryBlacklist {

        //TODO convert to a tag (or at least make compatible with) - I don't know whether this might or might not work
        public final ConfigValue<List<? extends String>> blockBlacklist;
        public final ConfigValue<List<? extends String>> blockWhitelist;
        private PatternList parsedBlacklist = null;
        private PatternList parsedWhitelist = null;

        private CategoryBlacklist() {
            SERVER_BUILDER.comment("Configure your Blacklist-Settings here")/*.translation(LANG_KEY_BLACKLIST)*/.push("Blacklist Settings");

            blockBlacklist = SERVER_BUILDER
                    .comment("All Blocks added to this will be treated similar to TileEntities. Not at all.",
                            "Notice that you can use Regular Expressions as defined by Java Patterns to express more complex name combinations.",
                            "Use for example \"awfulmod:.*\" to blacklist all awfulmod Blocks.")
                    .translation(LANG_KEY_BLACKLIST + ".blockBlacklist")
                    .defineList("Blacklisted Blocks", ImmutableList.of("minecraft:.*_door.*", PatternList.getName(Blocks.PISTON_HEAD)), obj -> obj instanceof String);

            blockWhitelist = SERVER_BUILDER
                    .comment("Allows you to define a whitelist, allowing Patterns to be defined in the same way as the blacklist.")
                    .translation(LANG_KEY_BLACKLIST + ".blockWhitelist")
                    .defineList("Whitelisted Blocks",ImmutableList.of(".*"),obj -> obj instanceof String);

            SERVER_BUILDER.pop();
        }

        private PatternList parsePatternListSafe(ConfigValue<List<? extends String>> value, List<String> def) {
            try {
                return PatternList.ofResourcePattern(value.get());
            } catch (PatternSyntaxException e) {
                BuildingGadgets.LOG
                        .error("The config value {}={} contains non-valid Pattern-Syntax. Defaulting to {}.", value
                                .getPath(), value.get(), def);
                return PatternList.ofResourcePattern(def);
            }
        }

        private void parseBlacklists() {
            parsedBlacklist = parsePatternListSafe(blockBlacklist, ImmutableList.of());
            parsedWhitelist = parsePatternListSafe(blockWhitelist, ImmutableList.of(".*"));
        }

        public boolean isAllowedBlock(Block block) {
            if (parsedBlacklist == null || parsedWhitelist == null) parseBlacklists();
            String regName = Objects.requireNonNull(block.getRegistryName()).toString();
            return parsedWhitelist.contains(regName) && !parsedBlacklist.contains(regName);
        }
    }

    public static final ForgeConfigSpec COMMON_CONFIG = COMMON_BUILDER.build();
    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();
    private static boolean serverCfgLoaded = false;

    private static void loadServerConfig() {
        BLACKLIST.parseBlacklists();
        serverCfgLoaded = true;
    }

    public static void onLoad(final ModConfig.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == Config.SERVER_CONFIG)
            loadServerConfig();
        BuildingGadgets.LOG.debug("Loaded {} config file {}", Reference.MODID, configEvent.getConfig().getFileName());
    }

    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        BuildingGadgets.LOG.fatal(CORE, "{} config just got changed on the file system!", Reference.MODID);
    }

    public static boolean isServerConfigLoaded() {
        return serverCfgLoaded;
    }
}

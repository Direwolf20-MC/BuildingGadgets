package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.Objects;

import static net.minecraftforge.common.ForgeConfigSpec.*;
import static net.minecraftforge.fml.Logging.CORE;

@EventBusSubscriber
public class Config {

    private static final String CATEGORY_GENERAL = "general";

    private static final String LANG_KEY_ROOT = "config." + Reference.MODID;

    private static final String LANG_KEY_GENERAL = LANG_KEY_ROOT + "." + CATEGORY_GENERAL;

    private static final String LANG_KEY_BLACKLIST = LANG_KEY_ROOT + ".blacklist";

    private static final String LANG_KEY_GADGETS = LANG_KEY_ROOT + ".gadgets";

    private static final String LANG_KEY_PASTE_CONTAINERS = LANG_KEY_ROOT + ".pasteContainers";

    private static final String LANG_KEY_PASTE_CONTAINERS_CAPACITY = LANG_KEY_PASTE_CONTAINERS + ".capacity";

    private static final String LANG_KEY_GADGET_BUILDING = LANG_KEY_GADGETS + ".gadgetBuilding";

    private static final String LANG_KEY_GADGET_EXCHANGER = LANG_KEY_GADGETS + ".gadgetExchanger";

    private static final String LANG_KEY_GADGET_DESTRUCTION = LANG_KEY_GADGETS + ".gadgetDestruction";

    private static final String LANG_KEY_GADGET_COPY_PASTE = LANG_KEY_GADGETS + ".gadgetCopyPaste";

    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final CategoryGadgets GADGETS = new CategoryGadgets();

    public static final CategoryPasteContainers PASTE_CONTAINERS = new CategoryPasteContainers();

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
            SERVER_BUILDER.comment("Configure the Gadgets")/*.translation(LANG_KEY_GADGETS)*/.push("Gadgets");

            maxRange = SERVER_BUILDER
                    .comment("The max range of the Gadgets")
                    .translation(LANG_KEY_GADGETS + ".maxRange")
                    .defineInRange("Maximum allowed Range", 15, 1, 32);

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

        private static IntValue getMaxEnergy(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The max energy of the Gadget")
                    .translation(LANG_KEY_GADGETS + ".maxEnergy")
                    .defineInRange("Maximum Energy", defaultValue, 1, Integer.MAX_VALUE);
        }

        private static IntValue getDurability(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The Gadget's Durability (0 means no durability is used) (Ignored if powered by FE)")
                    .translation(LANG_KEY_GADGETS + ".durability")
                    .defineInRange("Durability", defaultValue, 1, Integer.MAX_VALUE);
        }

        private static IntValue getEnergyCost(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The Gadget's Energy cost per Operation")
                    .translation(LANG_KEY_GADGETS + ".energyCost")
                    .defineInRange("Energy Cost", defaultValue, 0, Integer.MAX_VALUE);
        }

        private static IntValue getDurabilityCost(int defaultValue) {
            return SERVER_BUILDER
                    .comment("The Gadget's Durability cost per Operation")
                    .translation(LANG_KEY_GADGETS + ".durabilityCost")
                    .defineInRange("Durability Cost", defaultValue, 1, Integer.MAX_VALUE);
        }

        public static final class CategoryGadgetBuilding {

            public final IntValue maxEnergy, durability;

            public final IntValue energyCost, durabilityCost;

            private CategoryGadgetBuilding() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Building Gadget")/*.translation(LANG_KEY_GADGET_BUILDING)*/.push("Building Gadget");

                maxEnergy = getMaxEnergy(500000);
                durability = getDurability(500);

                energyCost = getEnergyCost(50);
                durabilityCost = getDurabilityCost(1);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetExchanger {

            public final IntValue maxEnergy, durability;

            public final IntValue energyCost, durabilityCost;

            private CategoryGadgetExchanger() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Exchanging Gadget")/*.translation(LANG_KEY_GADGET_EXCHANGER)*/.push("Exchanging Gadget");

                maxEnergy = getMaxEnergy(500000);
                durability = getDurability(500);

                energyCost = getEnergyCost(100);
                durabilityCost = getDurabilityCost(2);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetDestruction {

            public final IntValue maxEnergy, durability;

            public final IntValue energyCost, durabilityCost;

            public final DoubleValue nonFuzzyMultiplier;

            public final BooleanValue nonFuzzyEnabled;

            private CategoryGadgetDestruction() {
                SERVER_BUILDER.comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")/*.translation(LANG_KEY_GADGET_DESTRUCTION)*/.push("Destruction Gadget");

                maxEnergy = getMaxEnergy(1000000);
                durability = getDurability(1000);

                energyCost = getEnergyCost(200);
                durabilityCost = getDurabilityCost(4);

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

            public final IntValue maxEnergy, durability;

            public final IntValue energyCost, durabilityCost;


            private CategoryGadgetCopyPaste() {
                SERVER_BUILDER.comment("Energy Cost & Durability of the Copy-Paste Gadget")/*.translation(LANG_KEY_GADGET_COPY_PASTE)*/.push("Copy-Paste Gadget");

                maxEnergy = getMaxEnergy(500000);
                durability = getDurability(500);

                energyCost = getEnergyCost(50);
                durabilityCost = getDurabilityCost(1);

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
                    .defineInRange(String.format("T%s Container Capacity", tier), (int) (512 * Math.pow(2, tier - 1)), 1, Integer.MAX_VALUE);
        }
    }

    public static final class CategoryBlacklist {
        public final ConfigValue<List<? extends String>> blockBlacklist; //TODO convert to a tag (or at least make compatible with) - I don't know whether this might or might not work

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

        private void parseBlacklists() {
            parsedBlacklist = PatternList.ofResourcePattern(blockBlacklist.get());
            //TODO update once Forge allows trailing .'s
            parsedWhitelist = PatternList.ofResourcePattern(ImmutableList.of(".*")/*blockWhitelist.get()*/);
        }

        public boolean isAllowedBlock(Block block) {
            if (parsedBlacklist == null || parsedWhitelist == null) parseBlacklists();
            String regName = Objects.requireNonNull(block.getRegistryName()).toString();
            return parsedWhitelist.contains(regName) && !parsedBlacklist.contains(regName);
        }
    }

    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();

    public static void onLoad(final ModConfig.Loading configEvent) {
        BLACKLIST.parseBlacklists();
        BuildingGadgets.LOG.debug("Loaded {} config file {}", Reference.MODID, configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        BuildingGadgets.LOG.fatal(CORE, "{} config just got changed on the file system!", Reference.MODID);
    }

}

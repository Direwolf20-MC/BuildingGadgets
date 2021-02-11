package com.direwolf20.buildinggadgets.common.config;

import static net.minecraftforge.common.ForgeConfigSpec.*;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class Config {
    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();
    public static final CategoryGadgets GADGETS = new CategoryGadgets();
    public static final CategoryPasteContainers PASTE_CONTAINERS = new CategoryPasteContainers();

    public static final class CategoryGeneral {

        public final DoubleValue rayTraceRange;
        public final BooleanValue allowAbsoluteCoords;
        /* Client Only!*/
        public final BooleanValue absoluteCoordDefault;
        public final BooleanValue allowOverwriteBlocks;

        private CategoryGeneral() {
            SERVER_BUILDER.comment("General mod settings").push("general");
            CLIENT_BUILDER.comment("General mod settings").push("general");

            allowAbsoluteCoords = SERVER_BUILDER
                    .comment("Defined whether or not a player can use Absolute Coords mode in the Copy Paste Gadget")
                    .define("Allow Absolute Coords", true);

            rayTraceRange = SERVER_BUILDER
                    .comment("Defines how far away you can build")
                    .defineInRange("MaxBuildDistance", 32D, 1, 48);

            absoluteCoordDefault = CLIENT_BUILDER
                    .comment("Determines if the Copy/Paste GUI's coordinate mode starts in 'Absolute' mode by default.", "Set to true for Absolute, set to False for Relative.")
                    .define("Default to absolute Coord-Mode", false);

            allowOverwriteBlocks = SERVER_BUILDER
                    .comment("Whether the Building / CopyPaste Gadget can overwrite blocks like water, lava, grass, etc (like a player can).",
                            "False will only allow it to overwrite air blocks.")
                    .define("Allow non-Air-Block-Overwrite", true);

            CLIENT_BUILDER.pop();
            SERVER_BUILDER.pop();
        }
    }

    public static final class CategoryGadgets {
        public final IntValue maxRange;
        public final IntValue placeSteps;

        public final GadgetConfig GADGET_BUILDING;
        public final GadgetConfig GADGET_EXCHANGER;
        public final CategoryGadgetDestruction GADGET_DESTRUCTION;
        public final CategoryGadgetCopyPaste GADGET_COPY_PASTE;

        private CategoryGadgets() {
            SERVER_BUILDER.comment("Configure the Gadgets").push("Gadgets");

            maxRange = SERVER_BUILDER
                    .comment("The max range of the Gadgets")
                    .defineInRange("Maximum allowed Range", 15, 1, 32);

            //use the old cap as the synchronous border... This implies that 32*32*32 areas are the max size for a synchronous copy by default
            placeSteps = SERVER_BUILDER
                    .comment("Maximum amount of Blocks to be placed in one Tick.",
                            "Notice that an EffectBlock takes 20 ticks to place, therefore a Server has to handle 20-times this value effect-block Tile's at once. " +
                            "Reduce this if  you notice lag-spikes from Players placing Templates.",
                            "Of course decreasing this value will result in more time required to place large TemplateItem's.")
                    .defineInRange("Max Placement/Tick", 1024, 1, Integer.MAX_VALUE);

            GADGET_BUILDING = new GadgetConfig("Building Gadget", false, true, 500000, 2000, 50, 1, 10);
            GADGET_EXCHANGER = new GadgetConfig("Exchanging Gadget", false, true, 500000, 2000, 100, 1, 10);
            GADGET_DESTRUCTION = new CategoryGadgetDestruction();
            GADGET_COPY_PASTE = new CategoryGadgetCopyPaste();

            SERVER_BUILDER.pop();
        }

        public static class GadgetConfig {
            public final BooleanValue useDurability;
            public final BooleanValue useEnergy;
            public final IntValue maxEnergy;
            public final IntValue maxDurability;
            public final IntValue energyCost;
            public final IntValue durabilityCost;
            public final IntValue undoSize;

            public GadgetConfig(String name, boolean useDurability, boolean useEnergy, int maxEnergy, int maxDurability, int energyCost, int durabilityCost, int getMaxUndo) {
                SERVER_BUILDER.comment("Energy Cost & Durability of the " + name).push(name);

                this.useDurability = SERVER_BUILDER
                    .comment("Determines if the gadget uses durability")
                    .comment("If both Durability and Energy are set to false, the gadget will not use energy to power it.")
                    .worldRestart()
                    .define("Use Durability", useDurability);

                this.useEnergy = SERVER_BUILDER
                    .comment("Determines if the gadget uses energy")
                    .worldRestart()
                    .define("Use Energy", useEnergy);

                this.maxEnergy = SERVER_BUILDER
                        .comment("The max energy of the Gadget")
                        .comment("Only active when Use Energy is set to true")
                        .defineInRange("Maximum Energy", maxEnergy, 0, Integer.MAX_VALUE);

                this.maxDurability = SERVER_BUILDER
                    .comment("The max durability of the Gadget")
                    .comment("Only active when Use Durability is set to true")
                    .defineInRange("Maximum Durability", maxDurability, 0, Integer.MAX_VALUE);

                this.energyCost = SERVER_BUILDER
                        .comment("The Gadget's Energy cost per Operation (block placed, destroyed, exchanged)")
                        .comment("Only active when Use Energy is set to true")
                        .defineInRange("Energy Cost", energyCost, 0, Integer.MAX_VALUE);

                this.durabilityCost = SERVER_BUILDER
                    .comment("The Gadget's Durability cost per Operation (block placed, destroyed, exchanged)")
                    .comment("Only active when Use Durability is set to true")
                    .defineInRange("Durability Cost", durabilityCost, 0, Integer.MAX_VALUE);

                this.undoSize = SERVER_BUILDER
                        .comment("The Gadget's Max Undo size (Note, the exchanger does not support undo)")
                        .defineInRange("Max Undo History Size", getMaxUndo, 0, 128);

                SERVER_BUILDER.pop();
            }
        }

        public static final class CategoryGadgetDestruction extends GadgetConfig {
            public final IntValue destroySize;
            public final DoubleValue nonFuzzyMultiplier;
            public final BooleanValue nonFuzzyEnabled;

            private CategoryGadgetDestruction() {
                super("Destruction Gadget", false, true, 1000000, 4000, 200, 2, 1);

                SERVER_BUILDER
                        .comment("Energy Cost, Durability & Maximum Energy of the Destruction Gadget")
                        .push("Destruction Gadget");


                destroySize = SERVER_BUILDER
                        .comment("The maximum dimensions, the Destruction Gadget can destroy.")
                        .defineInRange("Destroy Dimensions", 16, 0, 32);

                nonFuzzyMultiplier = SERVER_BUILDER
                        .comment("The cost in energy/durability will increase by this amount when not in fuzzy mode")
                        .defineInRange("Non-Fuzzy Mode Multiplier", 2, 0, Double.MAX_VALUE);

                nonFuzzyEnabled = SERVER_BUILDER
                        .comment("If enabled, the Destruction Gadget can be taken out of fuzzy mode, allowing only instances of the block "
                                + "clicked to be removed (at a higher cost)")
                        .define("Non-Fuzzy Mode Enabled", false);

                SERVER_BUILDER.pop();

            }
        }

        public static final class CategoryGadgetCopyPaste extends GadgetConfig {
            public final IntValue copySteps;
            public final IntValue maxCopySize;
            public final IntValue maxBuildSize;

            private CategoryGadgetCopyPaste() {
                super("Copy-Paste Gadget", false, true, 500000, 2000,50, 1, 1);

                SERVER_BUILDER
                        .comment("Energy Cost & Durability of the Copy-Paste Gadget")
                        .push("Copy-Paste Gadget");

                //use the old cap as the per tick border... This implies that 32*32*32 areas are the max size for a one tick copy by default
                copySteps = SERVER_BUILDER
                        .comment("Maximum amount of Blocks to be copied in one Tick. ",
                                "Lower values may improve Server-Performance when copying large Templates")
                        .defineInRange("Max Copy/Tick", 32768, 1, Integer.MAX_VALUE);

                maxCopySize = SERVER_BUILDER
                        .comment("Maximum dimensions (x, y and z) that can be copied by a Template without requiring special permission.",
                                "Permission can be granted using the '/buildinggadgets OverrideCopySize [<Player>]' command.")
                        .defineInRange("Max Copy Dimensions", 256, - 1, Integer.MAX_VALUE);

                maxBuildSize = SERVER_BUILDER
                        .comment("Maximum dimensions (x, y and z) that can be build by a Template without requiring special permission.",
                                "Permission can be granted using the '/buildinggadgets OverrideBuildSize [<Player>]' command.")
                        .defineInRange("Max Build Dimensions", 256, - 1, Integer.MAX_VALUE);

                SERVER_BUILDER.pop();
            }
        }
    }

    public static final class CategoryPasteContainers {

        public final IntValue capacityT1, capacityT2, capacityT3;

        private CategoryPasteContainers() {
            SERVER_BUILDER
                    .comment("Configure the Paste Containers")
                    .push("Paste Containers");

            capacityT1 = getMaxCapacity(1);
            capacityT2 = getMaxCapacity(2);
            capacityT3 = getMaxCapacity(3);

            SERVER_BUILDER.pop();
        }

        private static IntValue getMaxCapacity(int tier) {
            return SERVER_BUILDER
                    .comment(String.format("The maximum capacity of a tier %s (iron) Construction Paste Container", tier))
                    .defineInRange(String.format("T%s Container Capacity", tier), (int) (512 * Math.pow(4, tier - 1)), 1, Integer.MAX_VALUE);
        }
    }

    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();
}

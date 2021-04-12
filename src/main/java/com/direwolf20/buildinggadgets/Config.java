package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.common.construction.UndoWorldStore;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraftforge.common.ForgeConfigSpec.*;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public final class Config {
    public static class ClientConfig {
        public ClientConfig(Builder builder) {

        }
    }

    public static class CommonConfig {
        public static IntValue gadgetsMaxUndos;
        public final IntValue gadgetMaxRange;
        public final IntValue gadgetRayTraceRange;
        public final BooleanValue allowBlockOverwrite;

        public CommonConfig(Builder builder) {
            builder.push("general");

            allowBlockOverwrite = builder
                    .comment("Defines whether or not partial blocks like grass can be replaced with the building block")
                    .define("allowBlockOverwrite", true);

            builder.pop();

            builder.push("gadgets");

            gadgetMaxRange = builder
                    .comment("Sets the max range that the gadgets can build up to. Setting it higher than the default max of 15 may cause odd building inconsistencies. You have been warned!")
                    .defineInRange("gadgetMaxRange", 15, 1, 32);

            gadgetRayTraceRange = builder
                    .comment("Sets the max distance you can select and build blocks from. Default is 20.")
                    .defineInRange("gadgetRayTraceRange", 20, 1, 100);

            gadgetsMaxUndos = builder
                    .comment("Sets how much history a gadget can store.")
                    .defineInRange("gadgetMaxUndos", 10, 1, 50);

            builder.pop();
        }
    }

    public static final ClientConfig CLIENT_CONFIG;
    public static final CommonConfig COMMON_CONFIG;

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> clientSpec = new Builder().configure(ClientConfig::new);
        final Pair<CommonConfig, ForgeConfigSpec> commonSpec = new Builder().configure(CommonConfig::new);

        CLIENT_CONFIG = clientSpec.getKey();
        CLIENT_SPEC = clientSpec.getValue();

        COMMON_CONFIG = commonSpec.getKey();
        COMMON_SPEC = commonSpec.getValue();
    }
}

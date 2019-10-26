package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public final class OverrideBuildSizeCommand {
    private static final AllowPlayerOverrideManager ALLOW_LARGE_BUILDS = new AllowPlayerOverrideManager(
            CommandTranslation.OVERRIDE_BUILD_SIZE_NO_PLAYER, CommandTranslation.OVERRIDE_BUILD_SIZE_TOGGLED,
            CommandTranslation.OVERRIDE_BUILD_SIZE_LIST, "override build size"
    );

    public static void toggleAllowLargeBuilds(PlayerEntity player) {
        ALLOW_LARGE_BUILDS.toggleAllowOverride(player);
    }

    public static void toggleAllowLargeBuilds(UUID uuid) {
        ALLOW_LARGE_BUILDS.toggleAllowOverride(uuid);
    }

    public static boolean mayPerformLargeBuild(UUID uuid) {
        return ALLOW_LARGE_BUILDS.mayOverride(uuid);
    }

    public static boolean mayPerformLargeBuild(PlayerEntity player) {
        return ALLOW_LARGE_BUILDS.mayOverride(player);
    }

    public static LiteralArgumentBuilder<CommandSource> registerToggle() {
        return Commands.literal("OverrideBuildSize")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")))
                );
    }

    public static LiteralArgumentBuilder<CommandSource> registerList() {
        return Commands.literal("OverrideBuildSizeList")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(OverrideBuildSizeCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSource> context, PlayerEntity player) {
        return ALLOW_LARGE_BUILDS.executeToggle(context, player);
    }

    private static int executeList(CommandContext<CommandSource> context) {
        return ALLOW_LARGE_BUILDS.executeList(context);
    }
}

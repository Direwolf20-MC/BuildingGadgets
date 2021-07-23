package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class OverrideBuildSizeCommand {
    private static final AllowPlayerOverrideManager ALLOW_LARGE_BUILDS = new AllowPlayerOverrideManager(
            CommandTranslation.OVERRIDE_BUILD_SIZE_NO_PLAYER, CommandTranslation.OVERRIDE_BUILD_SIZE_TOGGLED,
            CommandTranslation.OVERRIDE_BUILD_SIZE_LIST, "override build size"
    );

    public static void toggleAllowLargeBuilds(Player player) {
        ALLOW_LARGE_BUILDS.toggleAllowOverride(player);
    }

    public static void toggleAllowLargeBuilds(UUID uuid) {
        ALLOW_LARGE_BUILDS.toggleAllowOverride(uuid);
    }

    public static boolean mayPerformLargeBuild(UUID uuid) {
        return ALLOW_LARGE_BUILDS.mayOverride(uuid);
    }

    public static boolean mayPerformLargeBuild(Player player) {
        return ALLOW_LARGE_BUILDS.mayOverride(player);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerToggle() {
        return Commands.literal("OverrideBuildSize")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")))
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerList() {
        return Commands.literal("OverrideBuildSizeList")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(OverrideBuildSizeCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSourceStack> context, Player player) {
        return ALLOW_LARGE_BUILDS.executeToggle(context, player);
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        return ALLOW_LARGE_BUILDS.executeList(context);
    }
}

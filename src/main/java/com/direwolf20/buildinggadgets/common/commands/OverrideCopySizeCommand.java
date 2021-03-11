package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public final class OverrideCopySizeCommand {
    private static final AllowPlayerOverrideManager ALLOW_LARGE_COPIES = new AllowPlayerOverrideManager(
            CommandTranslation.OVERRIDE_COPY_SIZE_NO_PLAYER, CommandTranslation.OVERRIDE_COPY_SIZE_TOGGLED,
            CommandTranslation.OVERRIDE_COPY_SIZE_LIST, "override copy size"
    );

    public static void toggleAllowLargeCopies(PlayerEntity player) {
        ALLOW_LARGE_COPIES.toggleAllowOverride(player);
    }

    public static void toggleAllowLargeCopies(UUID uuid) {
        ALLOW_LARGE_COPIES.toggleAllowOverride(uuid);
    }

    public static boolean mayPerformLargeCopy(UUID uuid) {
        return ALLOW_LARGE_COPIES.mayOverride(uuid);
    }

    public static boolean mayPerformLargeCopy(PlayerEntity player) {
        return ALLOW_LARGE_COPIES.mayOverride(player);
    }

    public static LiteralArgumentBuilder<CommandSource> registerToggle() {
        return Commands.literal("OverrideCopySize")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")))
                );
    }

    public static LiteralArgumentBuilder<CommandSource> registerList() {
        return Commands.literal("OverrideCopySizeList")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(OverrideCopySizeCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSource> context, PlayerEntity player) {
        return ALLOW_LARGE_COPIES.executeToggle(context, player);
    }

    private static int executeList(CommandContext<CommandSource> context) {
        return ALLOW_LARGE_COPIES.executeList(context);
    }
}

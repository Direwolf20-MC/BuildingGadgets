package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class OverrideCopySizeCommand {
    private static final AllowPlayerOverrideManager ALLOW_LARGE_COPIES = new AllowPlayerOverrideManager(
            CommandTranslation.OVERRIDE_COPY_SIZE_NO_PLAYER, CommandTranslation.OVERRIDE_COPY_SIZE_TOGGLED,
            CommandTranslation.OVERRIDE_COPY_SIZE_LIST, "override copy size"
    );

    public static void toggleAllowLargeCopies(Player player) {
        ALLOW_LARGE_COPIES.toggleAllowOverride(player);
    }

    public static void toggleAllowLargeCopies(UUID uuid) {
        ALLOW_LARGE_COPIES.toggleAllowOverride(uuid);
    }

    public static boolean mayPerformLargeCopy(UUID uuid) {
        return ALLOW_LARGE_COPIES.mayOverride(uuid);
    }

    public static boolean mayPerformLargeCopy(Player player) {
        return ALLOW_LARGE_COPIES.mayOverride(player);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerToggle() {
        return Commands.literal("OverrideCopySize")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")))
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerList() {
        return Commands.literal("OverrideCopySizeList")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(OverrideCopySizeCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSourceStack> context, Player player) {
        return ALLOW_LARGE_COPIES.executeToggle(context, player);
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        return ALLOW_LARGE_COPIES.executeList(context);
    }
}

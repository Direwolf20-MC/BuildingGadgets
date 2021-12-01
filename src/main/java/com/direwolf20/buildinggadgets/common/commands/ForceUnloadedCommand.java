package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ForceUnloadedCommand {
    private static final AllowPlayerOverrideManager ALLOW_UNLOADED_CHUNKS = new AllowPlayerOverrideManager(
            CommandTranslation.FORCE_UNLOADED_NO_PLAYER, CommandTranslation.FORCE_UNLOADED_TOGGLED,
            CommandTranslation.FORCE_UNLOADED_LIST, "allow unloaded chunks"
    );

    public static void toggleAllowUnloadedChunks(Player player) {
        ALLOW_UNLOADED_CHUNKS.toggleAllowOverride(player);
    }

    public static void toggleAllowUnloadedChunks(UUID uuid) {
        ALLOW_UNLOADED_CHUNKS.toggleAllowOverride(uuid);
    }

    public static boolean mayForceUnloadedChunks(UUID uuid) {
        return ALLOW_UNLOADED_CHUNKS.mayOverride(uuid);
    }

    public static boolean mayForceUnloadedChunks(Player player) {
        return ALLOW_UNLOADED_CHUNKS.mayOverride(player);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerToggle() {
        return Commands.literal("ForceLoadChunks")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")))
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerList() {
        return Commands.literal("ForceLoadChunksList")
                .requires(commandSource -> commandSource.hasPermission(2))
                .executes(ForceUnloadedCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSourceStack> context, Player player) {
        return ALLOW_UNLOADED_CHUNKS.executeToggle(context, player);
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        return ALLOW_UNLOADED_CHUNKS.executeList(context);
    }
}

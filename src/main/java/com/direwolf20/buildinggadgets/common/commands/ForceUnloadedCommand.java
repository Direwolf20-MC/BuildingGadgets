package com.direwolf20.buildinggadgets.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class ForceUnloadedCommand {
    private static final AllowPlayerOverrideManager ALLOW_UNLOADED_CHUNKS = new AllowPlayerOverrideManager();

    public static void toggleAllowUnloadedChunks(PlayerEntity player) {
        ALLOW_UNLOADED_CHUNKS.toggleAllowOverride(player);
    }

    public static void toggleAllowUnloadedChunks(UUID uuid) {
        ALLOW_UNLOADED_CHUNKS.toggleAllowOverride(uuid);
    }

    public static boolean mayForceUnloadedChunks(UUID uuid) {
        return ALLOW_UNLOADED_CHUNKS.mayOverride(uuid);
    }

    public static boolean mayForceUnloadedChunks(PlayerEntity player) {
        return ALLOW_UNLOADED_CHUNKS.mayOverride(player);
    }

    public static LiteralArgumentBuilder<CommandSource> registerToggle() {
        return Commands.literal("ForceLoadChunks")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")))
                );
    }

    public static LiteralArgumentBuilder<CommandSource> registerList() {
        return Commands.literal("ForceLoadChunksList")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(ForceUnloadedCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSource> context, PlayerEntity player) {
        return ALLOW_UNLOADED_CHUNKS.executeToggle(context, player);
    }

    private static int executeList(CommandContext<CommandSource> context) {
        return ALLOW_UNLOADED_CHUNKS.executeList(context);
    }
}

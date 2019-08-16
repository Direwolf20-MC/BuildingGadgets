package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CopyUnloadedCommand {
    private static final Cache<UUID, Boolean> ALLOW_UNLOADED_CHUNKS = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<UUID, Boolean>) notification -> BuildingGadgets.LOG.info(
                    "Player with id {} was removed from the list of players who may {} copy unloaded chunks. He/She will need to run the command in order to be allowed to copy unloaded chunks.",
                    notification.getKey(), notification.getValue() ? "" : "not"))
            .build();

    public static void toggleAllowCopyUnloadedChunks(PlayerEntity player) {
        toggleAllowCopyUnloadedChunks(player.getUniqueID());
    }

    public static void toggleAllowCopyUnloadedChunks(UUID uuid) {
        try {
            ALLOW_UNLOADED_CHUNKS.put(uuid, ! ALLOW_UNLOADED_CHUNKS.get(uuid, () -> false));
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.warn("Failed to toggle 'allow copy unloaded chunks' for player {}", uuid, e);
        }
    }

    public static boolean mayCopyUnloadedChunks(UUID uuid) {
        Boolean res = ALLOW_UNLOADED_CHUNKS.getIfPresent(uuid);
        return res != null ? res : false;
    }

    public static boolean mayCopyUnloadedChunks(PlayerEntity player) {
        return mayCopyUnloadedChunks(player.getUniqueID());
    }

    public static LiteralArgumentBuilder<CommandSource> registerToggle() {
        return Commands.literal("ForceLoadChunks")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .then(Commands.argument("player", EntityArgument.player()))
                .executes(context -> executeToggle(context, EntityArgument.getPlayer(context, "player")));
    }

    public static LiteralArgumentBuilder<CommandSource> registerList() {
        return Commands.literal("ForceLoadChunksList")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(CopyUnloadedCommand::executeList);
    }

    private static int executeToggle(CommandContext<CommandSource> context, PlayerEntity player) {
        if (player == null) {
            context.getSource().sendErrorMessage(CommandTranslation.COPY_UNLOADED_NO_PLAYER.componentTranslation().setStyle(Styles.RED));
            return 0;
        }
        toggleAllowCopyUnloadedChunks(player);
        context.getSource().sendFeedback(CommandTranslation.COPY_UNLOADED_TOGGLED
                .componentTranslation(player.getDisplayName(), mayCopyUnloadedChunks(player))
                .setStyle(Styles.AQUA), true);
        return 1;
    }

    private static int executeList(CommandContext<CommandSource> context) {
        for (Map.Entry<UUID, Boolean> entry : ALLOW_UNLOADED_CHUNKS.asMap().entrySet()) {
            ITextComponent component = CommandTranslation.COPY_UNLOADED_LIST.componentTranslation(entry.getKey(), entry.getValue());
            component = entry.getValue() ? component.setStyle(Styles.BLUE) : component.setStyle(Styles.DK_GREEN);
            context.getSource().sendFeedback(component, true);
        }
        return 1;
    }
}

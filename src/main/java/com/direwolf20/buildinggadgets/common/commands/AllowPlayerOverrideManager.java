package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.lang.CommandTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

final class AllowPlayerOverrideManager {
    private final Cache<UUID, Boolean> allowPlayerOverrideCache;

    AllowPlayerOverrideManager() {
        allowPlayerOverrideCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener((RemovalListener<UUID, Boolean>) notification -> BuildingGadgets.LOG.info(
                        "Player with id {} was removed from the list of players who may {} paste unloaded chunks. He/She will need to run the command in order to be allowed to copy unloaded chunks.",
                        notification.getKey(), notification.getValue() ? "" : "not"))
                .build();
    }

    void toggleAllowOverride(PlayerEntity player) {
        toggleAllowOverride(player.getUniqueID());
    }

    void toggleAllowOverride(UUID uuid) {
        try {
            allowPlayerOverrideCache.put(uuid, ! allowPlayerOverrideCache.get(uuid, () -> false));
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.warn("Failed to toggle 'allow copy unloaded chunks' for player {}", uuid, e);
        }
    }

    boolean mayOverride(UUID uuid) {
        Boolean res = allowPlayerOverrideCache.getIfPresent(uuid);
        return res != null ? res : false;
    }

    boolean mayOverride(PlayerEntity player) {
        return mayOverride(player.getUniqueID());
    }

    int executeToggle(CommandContext<CommandSource> context, PlayerEntity player) {
        if (player == null) {
            context.getSource().sendErrorMessage(CommandTranslation.COPY_UNLOADED_NO_PLAYER.componentTranslation().setStyle(Styles.RED));
            return 0;
        }
        toggleAllowOverride(player);
        context.getSource().sendFeedback(CommandTranslation.COPY_UNLOADED_TOGGLED
                .componentTranslation(player.getDisplayName(), mayOverride(player))
                .setStyle(Styles.AQUA), true);
        return 1;
    }

    int executeList(CommandContext<CommandSource> context) {
        for (Map.Entry<UUID, Boolean> entry : allowPlayerOverrideCache.asMap().entrySet()) {
            ITextComponent component = CommandTranslation.COPY_UNLOADED_LIST.componentTranslation(entry.getKey(), entry.getValue());
            component = entry.getValue() ? component.setStyle(Styles.BLUE) : component.setStyle(Styles.DK_GREEN);
            context.getSource().sendFeedback(component, true);
        }
        return 1;
    }
}

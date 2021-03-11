package com.direwolf20.buildinggadgets.common.commands;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

final class AllowPlayerOverrideManager {
    private final Cache<UUID, Boolean> allowPlayerOverrideCache;
    private final ITranslationProvider noPlayerTranslation;
    private final ITranslationProvider toggledTranslation;
    private final ITranslationProvider listTranslation;
    private final String logMessage;

    public AllowPlayerOverrideManager(ITranslationProvider noPlayerTranslation, ITranslationProvider toggledTranslation, ITranslationProvider listTranslation, String logMessage) {
        allowPlayerOverrideCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener((RemovalListener<UUID, Boolean>) notification -> BuildingGadgets.LOG.info(
                        "Player with id {} was removed from the list of players for which '{}' is {} enabled. " +
                                "He/She will need to run the command again for '{}' to become active again.",
                        notification.getKey(), logMessage, notification.getValue() ? "" : "not", logMessage))
                .build();

        this.noPlayerTranslation = noPlayerTranslation;
        this.toggledTranslation = toggledTranslation;
        this.listTranslation = listTranslation;
        this.logMessage = logMessage;
    }

    void toggleAllowOverride(PlayerEntity player) {
        toggleAllowOverride(player.getUUID());
    }

    void toggleAllowOverride(UUID uuid) {
        try {
            allowPlayerOverrideCache.put(uuid, ! allowPlayerOverrideCache.get(uuid, () -> false));
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.warn("Failed to toggle '{}' for player {}", logMessage, uuid, e);
        }
    }

    boolean mayOverride(UUID uuid) {
        Boolean res = allowPlayerOverrideCache.getIfPresent(uuid);
        return res != null ? res : false;
    }

    boolean mayOverride(PlayerEntity player) {
        return mayOverride(player.getUUID());
    }

    int executeToggle(CommandContext<CommandSource> context, PlayerEntity player) {
        if (player == null) {
            context.getSource().sendFailure(noPlayerTranslation.componentTranslation().setStyle(Styles.RED));
            return 0;
        }
        toggleAllowOverride(player);
        context.getSource().sendSuccess(toggledTranslation.componentTranslation(player.getDisplayName(), mayOverride(player))
                .setStyle(Styles.AQUA), true);
        return 1;
    }

    int executeList(CommandContext<CommandSource> context) {
        for (Map.Entry<UUID, Boolean> entry : allowPlayerOverrideCache.asMap().entrySet()) {
            TranslationTextComponent component = listTranslation.componentTranslation(entry.getKey(), entry.getValue());
            component = (TranslationTextComponent) (entry.getValue() ? component.setStyle(Styles.BLUE) : component.setStyle(Styles.DK_GREEN));
            context.getSource().sendSuccess(component, true);
        }
        return 1;
    }
}

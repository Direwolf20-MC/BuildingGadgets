package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class CacheTemplateProvider implements ITemplateProvider {
    private final Cache<UUID, ITemplate> cache;
    private final Set<UUID> allocatedIds;

    CacheTemplateProvider() {
        this.cache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
        this.allocatedIds = new HashSet<>();
    }

    @Override
    @Nonnull
    public ITemplate getTemplateForKey(@Nonnull ITemplateKey key) {
        UUID id = key.getTemplateId(this::getFreeId);
        try {
            ITemplate template = cache.get(id, () -> key.createTemplate(id));
            if (key.requestUpdate(template))
                onUpdateRequested(id);
            else if (key.requestRemoteUpdate(template))
                onRemoteUpdateRequested(id, template);
            return template;
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to access Cache!", e);
        }
    }

    private UUID getFreeId() {
        UUID id = UUID.randomUUID();
        if (allocatedIds.contains(id))
            return getFreeId();
        onIdAllocated(id);
        return id;
    }

    private void onIdAllocated(UUID allocatedId) {
        this.allocatedIds.add(allocatedId);
        //TODO packet
    }

    private void onUpdateRequested(UUID id) {
        //TODO packet
    }

    private void onRemoteUpdateRequested(UUID id, ITemplate template) {
        //TODO packet
    }

    void clear() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
        this.allocatedIds.clear();
    }
}

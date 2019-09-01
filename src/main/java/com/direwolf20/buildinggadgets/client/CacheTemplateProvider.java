package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateIdAllocated;
import com.direwolf20.buildinggadgets.common.network.packets.SplitPacketUpdateTemplate;
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
            return cache.get(id, () -> key.createTemplate(id));
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to access Cache!", e);
        }
    }

    @Override
    public void setTemplate(ITemplateKey key, ITemplate template) {
        UUID id = key.getTemplateId(this::getFreeId);
        allocatedIds.add(id);
        cache.put(id, template);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        PacketHandler.sendToServer(new PacketRequestTemplate(key.getTemplateId(this::getFreeId)));
        return true;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key) {
        UUID id = key.getTemplateId(this::getFreeId);
        ITemplate template = cache.getIfPresent(id);
        if (template != null)
            PacketHandler.getSplitManager().sendToServer(new SplitPacketUpdateTemplate(id, template));
        return template != null;
    }

    public void onRemoteIdAllocated(UUID id) {
        this.allocatedIds.add(id);
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
        PacketHandler.sendToServer(new PacketTemplateIdAllocated(allocatedId));
    }

    void clear() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
        this.allocatedIds.clear();
    }
}

package com.direwolf20.buildinggadgets.client.cache;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.packets.PacketTemplateIdAllocated;
import com.direwolf20.buildinggadgets.common.network.packets.SplitPacketUpdateTemplate;
import com.direwolf20.buildinggadgets.common.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.template.Template;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class CacheTemplateProvider implements ITemplateProvider {
    private final Cache<UUID, Template> cache;
    private final Set<UUID> allocatedIds;
    private final Set<IUpdateListener> updateListeners;

    public CacheTemplateProvider() {
        this.cache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
        this.allocatedIds = new HashSet<>();
        this.updateListeners = Collections.newSetFromMap(new WeakHashMap<>());
    }

    @Override
    @Nonnull
    public Template getTemplateForKey(@Nonnull ITemplateKey key) {
        UUID id = getId(key);
        try {
            return cache.get(id, () -> {
                requestUpdate(id, PacketDistributor.SERVER.noArg());
                return new Template();
            });
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.error("Failed to access Cache! Returning new Template, this is certainly going to cause unexpected behaviour!", e);
            return new Template();
        }
    }

    @Override
    public void setTemplate(ITemplateKey key, Template template) {
        UUID id = getId(key);
        allocatedIds.add(id);
        cache.put(id, template);
        notifyListeners(key, template, l -> l::onTemplateUpdate);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return requestUpdate(key, PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean requestUpdate(ITemplateKey key, PacketTarget target) {
        return requestUpdate(key.getTemplateId(this::getFreeId), target);
    }

    private boolean requestUpdate(UUID id, PacketTarget target) {
        PacketHandler.send(new PacketRequestTemplate(id), target);
        return true;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, PacketTarget target) {
        UUID id = getId(key);
        Template template = cache.getIfPresent(id);
        if (template != null) {
            notifyListeners(key, template, l -> l::onTemplateUpdateSend);
            PacketHandler.getSplitManager().send(new SplitPacketUpdateTemplate(id, template), target);
        }
        return template != null;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key) {
        return requestRemoteUpdate(key, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void registerUpdateListener(IUpdateListener listener) {
        updateListeners.add(listener);
    }

    @Override
    public void removeUpdateListener(IUpdateListener listener) {
        updateListeners.remove(listener);
    }

    @Override
    public UUID getId(ITemplateKey key) {
        return key.getTemplateId(this::getFreeId);
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

    /**
     * Although public, do not use this method willingly. The cache is already purged on each
     * onPlayerLoggedOut event.
     */
    public void clear() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
        this.allocatedIds.clear();
    }

    private void notifyListeners(ITemplateKey key, Template template, Function<IUpdateListener, TriConsumer<ITemplateProvider, ITemplateKey, Template>> function) {
        for (IUpdateListener listener : updateListeners) {
            try {
                function.apply(listener).accept(this, key, template);
            } catch (Exception e) {
                BuildingGadgets.LOG.error("Update listener threw an exception!", e);
            }
        }
    }
}

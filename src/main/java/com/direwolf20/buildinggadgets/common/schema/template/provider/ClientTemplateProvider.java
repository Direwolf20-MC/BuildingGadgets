package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.packets.Packets;
import com.direwolf20.buildinggadgets.common.packets.RequestTemplatePacket;
import com.direwolf20.buildinggadgets.common.packets.UpdateTemplatePacket;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ClientTemplateProvider implements ITemplateProvider {
    private final Cache<UUID, Template> cachedTemplates;
    private final Cache<UUID, Consumer<Optional<Template>>> updateListeners;
    private final Set<UUID> pendingRequests;

    public ClientTemplateProvider() {
        this.cachedTemplates = CacheBuilder.newBuilder()
                .concurrencyLevel(1) //Only the client Thread will ever modify this
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
        this.updateListeners = CacheBuilder.newBuilder()
                .concurrencyLevel(1) //Only the client Thread will ever modify this
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .build();
        this.pendingRequests = new HashSet<>();
    }

    @Override
    public Optional<Template> getIfAvailable(UUID uuid) {
        Optional<Template> res = Optional.ofNullable(cachedTemplates.getIfPresent(uuid));
        if (! res.isPresent() && ! pendingRequests.contains(uuid)) {
            pendingRequests.add(uuid);
            Packets.sendToServer(new RequestTemplatePacket(uuid));
        }
        cleanUp();
        return res;
    }

    @Override
    public void getWhenAvailable(UUID id, Consumer<Optional<Template>> callback) {
        Optional<Template> current = getIfAvailable(id);
        if (current.isPresent())
            callback.accept(current);
        else {
            try {
                Consumer<Optional<Template>> cacheUpdate = updateListeners.get(id, () -> t -> {});
                updateListeners.put(id, t -> {cacheUpdate.accept(t); callback.accept(t);});
            } catch (ExecutionException e) {
                BuildingGadgets.LOGGER.error("Could not add callback for when an Template is available " +
                        "due to an unexpected exception! The callback will not be run!", e);
            }
        }
        cleanUp();
    }

    @Override
    public void setAndUpdateRemote(UUID id, @Nullable Template template, @Nullable PacketTarget sendTarget) {
        if (template != null)
            cachedTemplates.put(id, template);
        else
            cachedTemplates.invalidate(id);
        if (sendTarget != null)
            UpdateTemplatePacket.send(id, template, sendTarget);
        pendingRequests.remove(id);
        Consumer<Optional<Template>> listener = updateListeners.getIfPresent(id);
        updateListeners.invalidate(id);
        if (listener != null)
            listener.accept(Optional.ofNullable(template));
        cleanUp();
    }

    private void cleanUp() {
        cachedTemplates.cleanUp();
        updateListeners.cleanUp();
    }
}

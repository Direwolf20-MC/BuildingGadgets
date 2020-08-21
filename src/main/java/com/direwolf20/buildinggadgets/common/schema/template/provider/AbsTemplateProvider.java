package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import com.google.common.cache.*;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

/**
 * Base implementation of an {@link ITemplateProvider} which handles the update listeners registered to
 * {@link ITemplateProvider#registerUpdateCallback(UUID, BiPredicate)}.
 */
abstract class AbsTemplateProvider implements ITemplateProvider {
    private final LoadingCache<UUID, Set<BiPredicate<Optional<Template>, Boolean>>> updateListeners;

    public AbsTemplateProvider() {
        this.updateListeners = CacheBuilder.newBuilder()
                .concurrencyLevel(1) //Only the client Thread will ever modify this
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .removalListener(new RemovalListener<UUID, Set<BiPredicate<Optional<Template>, Boolean>>>() {
                    @Override
                    public void onRemoval(RemovalNotification<UUID, Set<BiPredicate<Optional<Template>, Boolean>>> notification) {
                        if (! notification.getValue().isEmpty()) {
                            Set<BiPredicate<Optional<Template>, Boolean>> resSet = new HashSet<>();
                            Optional<Template> template = getTemplate(notification.getKey());
                            for (BiPredicate<Optional<Template>, Boolean> fun : notification.getValue()) {
                                if (fun.test(template, true))
                                    resSet.add(fun);
                            }
                            if (! resSet.isEmpty()) //TODO test if this actually works, or whether it gives us an CME
                                updateListeners.put(notification.getKey(), resSet);
                        }
                    }
                })
                .build(new CacheLoader<UUID, Set<BiPredicate<Optional<Template>, Boolean>>>() {
                    @Override
                    public Set<BiPredicate<Optional<Template>, Boolean>> load(UUID key) throws Exception {
                        return new HashSet<>();
                    }
                });
    }

    @Override
    public void registerUpdateCallback(UUID id, BiPredicate<Optional<Template>, Boolean> callback) {
        try {
            Set<BiPredicate<Optional<Template>, Boolean>> current = updateListeners.get(id);
            current.add(callback);
        } catch (Exception e) {
            BuildingGadgets.LOGGER.error("Could not add callback for Template-Update of {}" +
                    "due to an unexpected exception! The callback will not be run!", id, e);
        }
    }

    @Override
    public void setAndUpdateRemote(UUID id, @Nullable Template template, @Nullable PacketDistributor.PacketTarget sendTarget) {
        Optional<Template> optTemp = Optional.ofNullable(template);
        try {
            Set<BiPredicate<Optional<Template>, Boolean>> listeners = updateListeners.get(id);
            listeners.removeIf(fun -> fun.test(optTemp, false));
            if (listeners.isEmpty())
                updateListeners.invalidate(id);
        } catch (Exception e) {
            BuildingGadgets.LOGGER.error("Unexpected exception delivering update to listeners of {}", id, e);
        }
        cleanUp();
    }

    /**
     * This get's the currently known {@link Template} value associated with the id. It may not request an update in
     * contrast to {@link #getIfAvailable(UUID)}.
     */
    protected abstract Optional<Template> getTemplate(UUID uuid);

    protected void cleanUp() {
        updateListeners.cleanUp();
    }
}
